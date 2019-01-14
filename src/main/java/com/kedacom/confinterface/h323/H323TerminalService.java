package com.kedacom.confinterface.h323;

import com.kedacom.confadapter.*;
import com.kedacom.confinterface.dto.BaseResponseMsg;
import com.kedacom.confinterface.exchange.*;
import com.kedacom.confinterface.inner.DetailMediaResouce;
import com.kedacom.confinterface.inner.MediaTypeEnum;
import com.kedacom.confinterface.inner.TransportAddress;
import com.kedacom.confinterface.inner.TransportDirectionEnum;
import com.kedacom.confinterface.service.TerminalService;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.*;
import java.util.concurrent.Future;

public class H323TerminalService extends TerminalService {

    public H323TerminalService(String e164, String name, boolean bVmt, H323ProtocalConfig h323ProtocalConfig) {
        super(e164, name, bVmt);
        this.regGK = false;
        this.gkIp = h323ProtocalConfig.getGkIp();
        this.gkRasPort = h323ProtocalConfig.getGkRasPort();
        this.localIp = h323ProtocalConfig.getBaseSysConfig().getLocalIp();
        this.localCallPort = h323ProtocalConfig.getLocalCallPort();
        this.localRasPort = h323ProtocalConfig.getLocalRasPort();
    }

    @Async("confTaskExecutor")
    public Future<Boolean> startRegGK() {
        if (regGK)
            return new AsyncResult<>(true);

        if (null == conferenceParticipant)
            return new AsyncResult<>(true);

        //注册Gk
        try {
            ConferenceSysRegisterInfo registerInfo = new ConferenceSysRegisterInfo();
            registerInfo.setId(e164);
            registerInfo.setName(name);
            registerInfo.setEndpointId(mtId);

            NetAddress gkAddress = new NetAddress();
            gkAddress.setIP(gkIp);
            gkAddress.setPort(gkRasPort);
            registerInfo.setConfRegServerAddr(gkAddress);

            NetAddress localAddress = new NetAddress();
            localAddress.setIP(localIp);
            localAddress.setPort(localCallPort);
            registerInfo.setCallLocalAddr(localAddress);

            NetAddress localRasAddress = new NetAddress();
            localRasAddress.setIP(localIp);
            localRasAddress.setPort(localRasPort);
            registerInfo.setRegClientAddr(localRasAddress);

            System.out.println("e164("+e164+") start register gk, time : "+System.currentTimeMillis()+", threadName:"+Thread.currentThread().getName());

            regGK = conferenceParticipant.RegisterToConfSys(registerInfo);
            if (regGK){
                System.out.println("register gk ok, time : "+System.currentTimeMillis());
                return new AsyncResult<>(true);
            } else {
                System.out.println("register gk failed, time : " + System.currentTimeMillis());
                return new AsyncResult<>(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AsyncResult<>(false);
        }
    }

    @Async("confTaskExecutor")
    public boolean onOpenLogicalChannel(Vector<MediaDescription> mediaDescriptions){
        /*1.判断是否存在反向通道资源，存在，则直接查询
          2. 不存在，则请求创建
        * */
        boolean bCreate = true;
        if (null != reverseChannel){
            List<String> resourceInfo = new ArrayList<>();
            List<UpdateResourceParam> updateResourceParams = new ArrayList<>();

            for (MediaDescription mediaDescription : mediaDescriptions){
                for (DetailMediaResouce detailMediaResouce : reverseChannel){
                    if (mediaDescription.getMediaType().equals(detailMediaResouce.getType())
                        && mediaDescription.GetStreamIndex() == detailMediaResouce.getDual()){
                        resourceInfo.add(detailMediaResouce.getId());
                        UpdateResourceParam updateResourceParam = new UpdateResourceParam(detailMediaResouce.getId());
                        updateResourceParam.setSdp(constructSdp(mediaDescription));
                        updateResourceParams.add(updateResourceParam);
                    }
                }
            }

            if (resourceInfo.size() > 0) {
                System.out.println("H323, onOpenLogicalChannel, exist reverseChannel info, start query exchange info, resourceInfoSize:"+resourceInfo.size());
                bCreate = false;
                List<ExchangeInfo> exchangeInfos = getExchange(resourceInfo);
                if (null == exchangeInfos)
                    return false;

                updateMediaResource(true, exchangeInfos);
                boolean bOk = requestUpdateResource(updateResourceParams);
                if (!bOk)
                    return false;
            }
        }

        if (bCreate){
            System.out.println("H323, onOpenLogicalChannel, start add exchange info!! threadName:"+Thread.currentThread().getName());

            for (MediaDescription mediaDescription : mediaDescriptions) {
                System.out.println("MediaType:"+mediaDescription.getMediaType()+", Direction:"+mediaDescription.getDirection()+", ChnlIndx:"+mediaDescription.getChannelIndex()+", Payload:"+mediaDescription.getPayload());
                System.out.println("Payload:"+mediaDescription.getPayload()+", EncodingName:"+mediaDescription.getEncodingFormat());
                System.out.println("Rtp["+mediaDescription.getRtpAddress().getIP()+":"+mediaDescription.getRtpAddress().getPort()+"]");
                System.out.println("Rtcp["+mediaDescription.getRtcpAddress().getIP()+":"+mediaDescription.getRtcpAddress().getPort()+"]");

                CreateResourceParam createResourceParam = new CreateResourceParam();
                createResourceParam.setSdp(constructSdp(mediaDescription));
                CreateResourceResponse resourceResponse = addExchange(createResourceParam);
                if (null == resourceResponse)
                    return false;
                addMediaResource(mediaDescription.GetStreamIndex(), resourceResponse);
            }
        }

        boolean bOk = ackOpenLogicalChannel(mediaDescriptions);
        if(bOk){
            //在收到反向的通道打开时，发起正向通道的打开
            System.out.println("H323, start open forward logical channel...........");
            bOk = openLogicalChannel(mediaDescriptions);
        }

        if (!bOk){
            //释放交换资源
            List<String> resourceIds = new ArrayList<>();
            for (DetailMediaResouce detailMediaResouce : reverseChannel){
                resourceIds.add(detailMediaResouce.getId());
            }
            removeExchange(resourceIds);
        }

        return bOk;
    }

    @Async("confTaskExecutor")
    public boolean openLogicalChannel(Vector<MediaDescription> mediaDescriptions){
        Vector<MediaDescription> newMediaDescription  = new Vector<>();

        boolean bCreate = true;
        if (null != forwardChannel){
            //查询资源信息，并打开通道
            List<String> resourceInfo = new ArrayList<>();
            for (MediaDescription mediaDescription : mediaDescriptions){
                for (DetailMediaResouce detailMediaResouce : forwardChannel){
                    if (mediaDescription.getMediaType().equals(detailMediaResouce.getType())
                            && mediaDescription.GetStreamIndex() == detailMediaResouce.getDual()){
                        resourceInfo.add(detailMediaResouce.getId());
                    }
                }
            }

            if (resourceInfo.size() > 0){
                System.out.println("H323, openLogicalChannel, exist forward channel info, query exchange info!!!");
                bCreate = false;

                List<ExchangeInfo> exchangeInfos = getExchange(resourceInfo);
                if (null == exchangeInfos)
                    return false;

                //更新正向媒体资源信息
                updateMediaResource(false, exchangeInfos);

                for (ExchangeInfo exchangeInfo : exchangeInfos){
                    for (DetailMediaResouce detailMediaResouce : forwardChannel){
                        if (!detailMediaResouce.getId().equals(exchangeInfo.getResourceID()))
                            continue;

                        /*todo:此处的for循环在赵智琛提供能力集协商结果后，需要去掉，同时修改constructRequestMedisDescription函数接口*/
                        for (MediaDescription mediaDescription : mediaDescriptions){
                            if (!mediaDescription.getMediaType().equals(detailMediaResouce.getType()))
                                continue;
                            if (mediaDescription.GetStreamIndex() != detailMediaResouce.getDual())
                                continue;

                            MediaDescription requestRemoteMedia = constructRequestMediaDescription(mediaDescription, exchangeInfo.getLocalSdp());
                            newMediaDescription.add(requestRemoteMedia);
                            break;
                        }//for
                        break;
                    }//for
                }//for
            }
        }

        if (bCreate){
            System.out.println("H323, openLogicalChannel, start add exchange info!!! threadName:"+Thread.currentThread().getName());

            for (MediaDescription mediaDescription : mediaDescriptions) {
                CreateResourceParam createResourceParam = new CreateResourceParam();
                createResourceParam.setSdp(constructCreateSdp(mediaDescription));
                CreateResourceResponse resourceResponse = addExchange(createResourceParam);
                if (null == resourceResponse)
                    return false;

                addMediaResource(mediaDescription.GetStreamIndex(), resourceResponse);

                MediaDescription requestRemoteMedia = constructRequestMediaDescription(mediaDescription, resourceResponse.getSdp());
                newMediaDescription.add(requestRemoteMedia);
            }
        }

        boolean bOk = conferenceParticipant.RequestRemoteMedia(newMediaDescription);
        if (!bOk) {
            System.out.println("H323, openLogicalChannel, RequestRemoteMedia failed! participartId : " + e164);
            //释放交换资源
            List<String> resourceIds = new ArrayList<>();
            for (DetailMediaResouce detailMediaResouce : forwardChannel){
                resourceIds.add(detailMediaResouce.getId());
            }

            removeExchange(resourceIds);
            return false;
        }

        System.out.println("H323, openLogicalChannel, RequestRemoteMedia OK! participartId : " + e164);
        return true;
    }

    @Async("confTaskExecutor")
    public boolean updateExchange(Vector<MediaDescription> mediaDescriptions){
        System.out.println("now in updateExchange, mediaDescriptions : "+mediaDescriptions.size());
        List<UpdateResourceParam> updateResourceParams = new ArrayList<>();
        List<DetailMediaResouce> channel;

        for (MediaDescription mediaDescription : mediaDescriptions){
            System.out.println("updateExchange, mediaDescription, direction : " + mediaDescription.getDirection());
            if (TransportDirectionEnum.SEND.getName().equals(mediaDescription.getDirection())){
                //反向通道更新
                channel = reverseChannel;
            } else {
                channel = forwardChannel;
            }

            for (DetailMediaResouce detailMediaResouce : channel) {
                System.out.println("updateExchange, mediaDesc(mediaType:"+mediaDescription.getMediaType()+", streamIndex:"+mediaDescription.GetStreamIndex()+")");
                System.out.println("                detailMediaResource(type:"+detailMediaResouce.getType()+", dual:"+detailMediaResouce.getDual()+")");

                if (!mediaDescription.getMediaType().equals(detailMediaResouce.getType()))
                    continue;

                if (mediaDescription.GetStreamIndex() != detailMediaResouce.getDual())
                    continue;

                UpdateResourceParam updateResourceParam = new UpdateResourceParam(detailMediaResouce.getId());
                updateResourceParam.setSdp(constructSdp(mediaDescription));

                updateResourceParams.add(updateResourceParam);
                break;
            }
        }

        return requestUpdateResource(updateResourceParams);
    }

    private boolean requestUpdateResource(List<UpdateResourceParam> updateResourceParams){
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/services/media/v1/exchange?GroupID={groupId}&Action=updatenode");
        Map<String, String> args = new HashMap<>();
        args.put("groupId", groupId);

        System.out.println("requestUpdateResource, groupId:"+groupId+", updateResourceSize:"+updateResourceParams.size());

        for (UpdateResourceParam updateResourceParam : updateResourceParams){
            System.out.println("requestUpdateResource, start update exchange, resourceId:"+updateResourceParam.getResourceID()+", sdp:"+updateResourceParam.getSdp());
            ResponseEntity<BaseResponseMsg> updateResponse = restClientService.exchangeJson(url.toString(), HttpMethod.POST, updateResourceParam, args, BaseResponseMsg.class);
            if (!updateResponse.getStatusCode().is2xxSuccessful()){
                System.out.println("requestUpdateResource, update node failed! , resourceId:"+updateResourceParam.getResourceID()+", status : "+updateResponse.getStatusCodeValue()+", url:"+url.toString());
                return false;
            }

            if (updateResponse.getBody().getCode() != 0){
                System.out.println("requestUpdateResource, update exchange failed, resourceId:"+updateResourceParam.getResourceID()+", messge:"+updateResponse.getBody().getMessage());
                return false;
            }

            System.out.println("requestUpdateResource, update exchange OK! resourceId:"+updateResourceParam.getResourceID());
        }

        return true;
    }

    private String constructCreateSdp(MediaDescription mediaDescription){
        StringBuilder sdp = new StringBuilder();

        /*此处暂时将mcu向会议接入微服务打开逻辑通道时使用的媒体参数作为接受媒体信息携带的流媒体
        * todo:等到赵智琛将能力集协商结果提供出来后，此处可以需填写能力集协商内容*/
        if (mediaDescription.getMediaType().equals(MediaTypeEnum.VIDEO.getName())){
            sdp.append("m=video 0 RTP/AVP ");
            sdp.append(mediaDescription.getPayload());
            VideoMediaDescription videoMediaDescription = (VideoMediaDescription)mediaDescription;
            sdp.append("a=rtpmap:");
            sdp.append(mediaDescription.getPayload());
            sdp.append(" ");
            sdp.append(mediaDescription.getEncodingFormat());
            sdp.append("/90000\r\n");
            sdp.append("a=framerate:");
            sdp.append(videoMediaDescription.getFramerate());

            if (mediaDescription.getEncodingFormat().equals(VideoMediaDescription.ENCODING_FORMAT_H264)){
                constructH264Fmtp(sdp, mediaDescription.getPayload(), videoMediaDescription.getH264Desc());
            }
        } else {
            sdp.append("m=audio 0 RTP/AVP ");
            sdp.append(mediaDescription.getPayload());
            AudioMediaDescription audioMediaDescription = (AudioMediaDescription)mediaDescription;
            sdp.append("a=rtpmap:");
            sdp.append(mediaDescription.getPayload());
            sdp.append(" ");
            sdp.append(mediaDescription.getEncodingFormat());
            sdp.append("/");
            sdp.append(audioMediaDescription.getSampleRate());
            sdp.append("/");
            sdp.append(audioMediaDescription.getChannelNum());
            sdp.append("\r\n");
        }

        sdp.append("a=recvonly\r\n");

        return sdp.toString();
    }

    /*todo：等赵智琛提供了能力集协商结果后，此处应该根据exchangeSdp中的payload在能力集协商结果中选择相应的mediaDescription*/
    private MediaDescription constructRequestMediaDescription(MediaDescription mediaDescription, String exchangeSdp){
        MediaDescription newMediaDescription;

        if (exchangeSdp.contains("m=video")){
            VideoMediaDescription videoMediaDescription = new VideoMediaDescription();
            videoMediaDescription.setResolution(((VideoMediaDescription)mediaDescription).getResolution());
            videoMediaDescription.setFramerate(((VideoMediaDescription)mediaDescription).getFramerate());
            videoMediaDescription.setBitrateType(((VideoMediaDescription)mediaDescription).getBitrateType());
            newMediaDescription = videoMediaDescription;
            newMediaDescription.setMediaType(MediaTypeEnum.VIDEO.getName());
        } else {
            AudioMediaDescription audioMediaDescription = new AudioMediaDescription();
            audioMediaDescription.setSampleRate(((AudioMediaDescription)mediaDescription).getSampleRate());
            audioMediaDescription.setChannelNum(((AudioMediaDescription)mediaDescription).getChannelNum());
            newMediaDescription = audioMediaDescription;
            newMediaDescription.setMediaType(MediaTypeEnum.AUDIO.getName());
        }

        newMediaDescription.setBitrate(mediaDescription.getBitrate());
        newMediaDescription.setStreamIndex(mediaDescription.GetStreamIndex());
        if (exchangeSdp.contains("sendonly")){
            newMediaDescription.setDirection(TransportDirectionEnum.SEND.getName());
        } else {
            newMediaDescription.setDirection(TransportDirectionEnum.RECV.getName());
        }

        TransportAddress rtpAddress = constructTransAddress(exchangeSdp);
        NetAddress rtpNetAddress = new NetAddress();
        rtpNetAddress.setIP(rtpAddress.getIp());
        rtpNetAddress.setPort(rtpAddress.getPort());
        newMediaDescription.setRtpAddress(rtpNetAddress);

        NetAddress rtcpNetAddress = new NetAddress();
        rtcpNetAddress.setIP(rtpAddress.getIp());
        rtcpNetAddress.setPort(rtpAddress.getPort()+1);
        newMediaDescription.setRtcpAddress(rtcpNetAddress);

        parseRtpMapAndFmtp(exchangeSdp, newMediaDescription);

        return newMediaDescription;
    }

    public boolean isRegGK() {
        return regGK;
    }

    public String getGkIp() {
        return gkIp;
    }

    public void setGkIp(String gkIp) {
        this.gkIp = gkIp;
    }

    public int getGkRasPort() {
        return gkRasPort;
    }

    public void setGkRasPort(int gkRasPort) {
        this.gkRasPort = gkRasPort;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public int getLocalRasPort() {
        return localRasPort;
    }

    public void setLocalRasPort(int localRasPort) {
        this.localRasPort = localRasPort;
    }

    public int getLocalCallPort() {
        return localCallPort;
    }

    public void setLocalCallPort(int localCallPort) {
        this.localCallPort = localCallPort;
    }

    private boolean regGK;
    private String gkIp;
    private int gkRasPort;
    private String localIp;
    private int localRasPort;
    private int localCallPort;
}
