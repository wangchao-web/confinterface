package com.kedacom.confinterface.h323;

import com.kedacom.confadapter.*;
import com.kedacom.confinterface.dto.BaseRequestMsg;
import com.kedacom.confinterface.exchange.*;
import com.kedacom.confinterface.inner.DetailMediaResouce;
import com.kedacom.confinterface.inner.MediaTypeEnum;
import com.kedacom.confinterface.service.TerminalService;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
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
        this.videoDualStreamMediaDesc = null;
    }

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

    public boolean onOpenLogicalChannel(Vector<MediaDescription> mediaDescriptions){
        /*1.判断是否存在反向通道资源，存在，则直接查询
          2. 不存在，则请求创建
        * */
        boolean bCreate = true;
        boolean bOnlyDualStream = true;
        if (null != reverseChannel){
            List<String> resourceInfo = new ArrayList<>();
            List<UpdateResourceParam> updateResourceParams = new ArrayList<>();

            for (MediaDescription mediaDescription : mediaDescriptions){
                for (DetailMediaResouce detailMediaResouce : reverseChannel) {
                    if (!mediaDescription.getMediaType().equals(detailMediaResouce.getType()))
                        continue;

                    int streamIndex = detailMediaResouce.getStreamIndex();
                    if (-1 == streamIndex) {
                        //只有在异常重启后,回复上次的信息时,才会出现该情况
                        streamIndex = mediaDescription.getStreamIndex();
                        detailMediaResouce.setStreamIndex(streamIndex);
                        detailMediaResouce.setDual(mediaDescription.getDual());
                    }

                    if (!mediaDescription.getDual()) {
                        bOnlyDualStream = false;
                        if (null == videoDualStreamMediaDesc && mediaDescription.getMediaType().equals(MediaTypeEnum.VIDEO.getName())) {
                            videoDualStreamMediaDesc = mediaDescription;
                        }
                    }

                    if (mediaDescription.getStreamIndex() == streamIndex) {
                        resourceInfo.add(detailMediaResouce.getId());
                        UpdateResourceParam updateResourceParam = new UpdateResourceParam(detailMediaResouce.getId());
                        updateResourceParam.setSdp(constructSdp(mediaDescription));
                        updateResourceParams.add(updateResourceParam);
                        break;
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
        } else {
            for (MediaDescription mediaDescription : mediaDescriptions){
                if (!mediaDescription.getDual()){
                    bOnlyDualStream = false;
                    if (null != videoDualStreamMediaDesc || !mediaDescription.getMediaType().equals(MediaTypeEnum.VIDEO.getName())) {
                        continue;
                    }

                    videoDualStreamMediaDesc = mediaDescription;
                    break;
                } else if (null == videoDualStreamMediaDesc && mediaDescription.getMediaType().equals(MediaTypeEnum.VIDEO.getName())) {
                    videoDualStreamMediaDesc = mediaDescription;
                }
            }
        }

        if (bCreate){
            System.out.println("H323, onOpenLogicalChannel, start add exchange info!! threadName:"+Thread.currentThread().getName());

            for (MediaDescription mediaDescription : mediaDescriptions) {
                System.out.println("MediaType:"+mediaDescription.getMediaType()+", Direction:"+mediaDescription.getDirection()+", StreamIndx:"+mediaDescription.getStreamIndex()+", Payload:"+mediaDescription.getPayload());
                System.out.println("Payload:"+mediaDescription.getPayload()+", EncodingName:"+mediaDescription.getEncodingFormat());
                System.out.println("Rtp["+mediaDescription.getRtpAddress().getIP()+":"+mediaDescription.getRtpAddress().getPort()+"]");
                System.out.println("Rtcp["+mediaDescription.getRtcpAddress().getIP()+":"+mediaDescription.getRtcpAddress().getPort()+"]");

                CreateResourceParam createResourceParam = new CreateResourceParam();
                createResourceParam.setSdp(constructSdp(mediaDescription));
                CreateResourceResponse resourceResponse = addExchange(createResourceParam);
                if (null == resourceResponse)
                    return false;
                addMediaResource(mediaDescription.getStreamIndex(), mediaDescription.getDual(), resourceResponse);
            }
        }

        boolean bOk = ackOpenLogicalChannel(mediaDescriptions);
        if(bOk && !bOnlyDualStream){
            //在收到主流的反向的通道打开时，发起正向主流通道的打开
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

    public boolean openLogicalChannel(Vector<MediaDescription> mediaDescriptions){
        Vector<MediaDescription> newMediaDescription  = new Vector<>();

        boolean bCreate = true;
        if (null != forwardChannel){
            //查询资源信息，并打开通道
            List<String> resourceInfo = new ArrayList<>();
            for (MediaDescription mediaDescription : mediaDescriptions){
                for (DetailMediaResouce detailMediaResouce : forwardChannel){
                    if (!mediaDescription.getMediaType().equals(detailMediaResouce.getType())){
                        continue;
                    }

                    int streamIndex = detailMediaResouce.getStreamIndex();
                    if (-1 == streamIndex){
                        streamIndex = mediaDescription.getStreamIndex();
                        detailMediaResouce.setStreamIndex(streamIndex);
                    }

                    if (mediaDescription.getStreamIndex() == streamIndex){
                        resourceInfo.add(detailMediaResouce.getId());
						break;
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
                            if (mediaDescription.getStreamIndex() != detailMediaResouce.getStreamIndex())
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

                addMediaResource(mediaDescription.getStreamIndex(), mediaDescription.getDual(), resourceResponse);

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

    public void openDualStreamChannel(BaseRequestMsg startDualStreamRequest){
        if (null != forwardChannel)
            for (DetailMediaResouce detailMediaResouce : forwardChannel) {
                if (detailMediaResouce.getDual() == 1) {
                    startDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.EXIST_DUALSTREAM.getCode(), HttpStatus.OK, ConfInterfaceResult.EXIST_DUALSTREAM.getMessage());
                    return;
                }
            }

        videoDualStreamMediaDesc.setStreamIndex(-1);
        videoDualStreamMediaDesc.setDual(true);
        CreateResourceParam createResourceParam = new CreateResourceParam();
        createResourceParam.setSdp(constructCreateSdp(videoDualStreamMediaDesc));
        CreateResourceResponse resourceResponse = addExchange(createResourceParam);
        if (null == resourceResponse) {
            System.out.println("openDualStreamChannel, addExchange failed!");
            startDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.ADD_EXCHANGENODE_FAILED.getCode(), HttpStatus.OK, ConfInterfaceResult.ADD_EXCHANGENODE_FAILED.getMessage());
            return;
        }

        Vector<MediaDescription> newMediaDescription = new Vector<>();
        MediaDescription requestRemoteMedia = constructRequestMediaDescription(videoDualStreamMediaDesc, resourceResponse.getSdp());
        newMediaDescription.add(requestRemoteMedia);

        boolean bOk = conferenceParticipant.RequestRemoteMedia(newMediaDescription);
        if (!bOk) {
            System.out.println("H323, openDualStreamChannel, RequestRemoteMedia failed! participartId : " + e164);
            //释放交换资源
            List<String> resourceIds = new ArrayList<>();
            resourceIds.add(resourceResponse.getResourceID());
            removeExchange(resourceIds);
            return;
        }

        int dualStreamIndex = newMediaDescription.get(0).getStreamIndex();
        videoDualStreamMediaDesc.setStreamIndex(dualStreamIndex);
        addWaitMsg(startDualStreamRequest.getClass().getName(), startDualStreamRequest);
        addMediaResource(dualStreamIndex, videoDualStreamMediaDesc.getDual(), resourceResponse);

        System.out.println("openDualStreamChannel, RequestRemoteMedia Ok! streamIndex:"+dualStreamIndex);
    }

    public boolean closeDualStreamChannel(){
        if (null == videoDualStreamMediaDesc)
            return true;

        System.out.println("closeDualStreamChannel, streamIndex : "+videoDualStreamMediaDesc.getStreamIndex());
        boolean bOk = conferenceParticipant.RequestCleanupMediaById(videoDualStreamMediaDesc.getStreamIndex());
        if (!bOk){
            System.out.println("closeDualStreamChannel, RequestCleanupMediaById failed!");
            return false;
        }

        for (DetailMediaResouce detailMediaResouce : forwardChannel){
            if (detailMediaResouce.getStreamIndex() != videoDualStreamMediaDesc.getStreamIndex())
                continue;

            List<String> resourceIds = new ArrayList<>();
            resourceIds.add(detailMediaResouce.getId());

            removeExchange(resourceIds);

            forwardChannel.remove(detailMediaResouce);

            return true;
        }

        return true;
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
    protected MediaDescription videoDualStreamMediaDesc;
}
