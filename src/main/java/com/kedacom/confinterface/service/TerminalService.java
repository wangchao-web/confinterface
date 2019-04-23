package com.kedacom.confinterface.service;

import com.kedacom.confadapter.*;
import com.kedacom.confinterface.dao.InspectionSrcParam;
import com.kedacom.confinterface.dto.*;
import com.kedacom.confinterface.exchange.*;
import com.kedacom.confinterface.inner.*;
import com.kedacom.confinterface.restclient.RestClientService;
import com.kedacom.confinterface.restclient.mcu.InspectionStatusEnum;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TerminalService {

    public TerminalService(String e164, String name, boolean bVmt) {
        super();
        this.e164 = e164;
        this.mtId = null;
        this.groupId = null;
        this.name = name;
        this.online = new AtomicInteger();
        this.online.set(TerminalOnlineStatusEnum.UNKNOWN.getCode());
        this.inspectionStatus = new AtomicInteger();
        this.inspectionStatus.set(InspectionStatusEnum.UNKNOWN.getCode());
        this.inspectedStatus = new AtomicInteger();
        this.inspectedStatus.set(InspectionStatusEnum.UNKNOWN.getCode());
        this.toBeSpeaker = new AtomicBoolean(false);
        this.supportDualStream = new AtomicBoolean(false);
        this.inspectVideoStatus = new AtomicInteger();
        this.inspectVideoStatus.set(InspectionStatusEnum.UNKNOWN.getCode());
        this.inspectAudioStatus = new AtomicInteger();
        this.inspectAudioStatus.set(InspectionStatusEnum.UNKNOWN.getCode());
        this.confId = null;
        if (bVmt)
            this.type = 2;
        else
            this.type = 1;
        this.inspectionParam = null;
        this.inspentedTerminals = null;
        this.forwardChannel = null;
        this.reverseChannel = null;
        this.conferenceParticipant = null;
        this.waitMsg = null;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getE164() {
        return e164;
    }

    public void setE164(String e164) {
        this.e164 = e164;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfId() {
        return confId;
    }

    public void setConfId(String confId) {
        this.confId = confId;
    }

    public String getMtId() {
        return mtId;
    }

    public void setMtId(String mtId) {
        this.mtId = mtId;
    }

    public int getType() {
        return type;
    }

    public boolean isVmt() {
        return  (type == 2);
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setInspectionParam(InspectionSrcParam inspectionParam) {
        this.inspectionParam = inspectionParam;
    }

    public InspectionSrcParam getInspectionParam() {
        return inspectionParam;
    }

    public List<DetailMediaResouce> getForwardChannel() {
        return forwardChannel;
    }

    public List<DetailMediaResouce> getReverseChannel() {
        return reverseChannel;
    }

    public void setForwardChannel(CopyOnWriteArrayList<DetailMediaResouce> forwardChannel) {
        this.forwardChannel = forwardChannel;
    }

    public void setReverseChannel(CopyOnWriteArrayList<DetailMediaResouce> reverseChannel) {
        this.reverseChannel = reverseChannel;
    }

    public void addForwardChannel(DetailMediaResouce detailMediaResouce) {
        synchronized (this) {
            if (null == forwardChannel) {
                forwardChannel = new CopyOnWriteArrayList<>();
            }
        }

        forwardChannel.add(detailMediaResouce);
    }

    public void addReverseChannel(DetailMediaResouce detailMediaResouce) {
        synchronized (this) {
            if (null == reverseChannel) {
                reverseChannel = new CopyOnWriteArrayList<>();
            }
        }

        reverseChannel.add(detailMediaResouce);
    }

    public void addInspentedTerminal(String e164, InspectedParam inspectedParam) {
        synchronized (this) {
            if (null == inspentedTerminals) {
                inspentedTerminals = new ConcurrentHashMap<>();
            }
        }

        inspentedTerminals.put(e164, inspectedParam);
    }

    public void delInspentedTerminal(String e164) {
        if (null == e164 || null == inspentedTerminals)
            return;

        synchronized (inspentedTerminals) {
            inspentedTerminals.remove(e164);
            if (inspentedTerminals.isEmpty())
                inspectedStatus.set(InspectionStatusEnum.UNKNOWN.getCode());
        }
    }

   public InspectedParam getInspectedParam(String e164){
        if (null == inspentedTerminals)
            return null;

        return inspentedTerminals.get(e164);
   }

    public ConcurrentHashMap<String, InspectedParam> getInspentedTerminals() {
        return inspentedTerminals;
    }

    public void setInspectedStatus(String e164, InspectionStatusEnum status){
        if (null == inspentedTerminals)
            return;

        synchronized (inspentedTerminals) {
            InspectedParam inspectedParam = inspentedTerminals.get(e164);
            if (null == inspectedParam)
                return;

            inspectedParam.setStatus(status);
        }
    }

    public void setOnline(int online) {
        this.online.set(online);
    }

    public boolean isOnline() {
        return (this.online.get() == TerminalOnlineStatusEnum.ONLINE.getCode());
    }

    public boolean isOccupied(){
        return (this.online.get() == TerminalOnlineStatusEnum.OCCUPIED.getCode());
    }

    public String getOccupyConfName() {
        return occupyConfName;
    }

    public void setOccupyConfName(String occupyConfName) {
        this.occupyConfName = occupyConfName;
    }

    public boolean isInspected() {
        return (inspectedStatus.get() == InspectionStatusEnum.OK.getCode());
    }

    public boolean isInspection() {
        return (inspectionStatus.get() == InspectionStatusEnum.OK.getCode());
    }

    public boolean isToBeSpeaker() {
        return toBeSpeaker.get();
    }

    public void setToBeSpeaker(boolean toBeSpeaker) {
        this.toBeSpeaker.set(toBeSpeaker);
    }

    public void setSupportDualStream(boolean dualStream) {
        this.supportDualStream.set(dualStream);

    }

    public void allowExtensiveStream(){
        if (supportDualStream.get())
            conferenceParticipant.AllowAcceptExtensiveStreamRequest(true);
        else
            conferenceParticipant.AllowAcceptExtensiveStreamRequest(false);
    }

    public void setInspectedStatus(InspectionStatusEnum inspected) {
        this.inspectedStatus.set(inspected.getCode());
    }

    public void setInspectionStatus(InspectionStatusEnum inspection) {
        this.inspectionStatus.set(inspection.getCode());
    }

    public int getInspectAudioStatus() {
        return inspectAudioStatus.get();
    }

    public int getInspectVideoStatus() {
        return inspectVideoStatus.get();
    }

    public void setInspectAudioStatus(int inspectAudioStatus) {
        this.inspectAudioStatus.set(inspectAudioStatus);
    }

    public void setInspectVideoStatus(int inspectVideoStatus) {
        this.inspectVideoStatus.set(inspectVideoStatus);
    }

    public boolean existInspectFail(){
        if (inspectVideoStatus.get() == InspectionStatusEnum.FAIL.getCode())
            return true;

        return (inspectAudioStatus.get() == InspectionStatusEnum.FAIL.getCode());
    }

    public int getInspectStatus(int mode){
        if (mode == InspectionModeEnum.VIDEO.getCode())
            return inspectVideoStatus.get();
        if (mode == InspectionModeEnum.AUDIO.getCode())
            return inspectAudioStatus.get();

        return InspectionStatusEnum.UNKNOWN.getCode();
    }

    public boolean isInspectionFail(){
        return (InspectionStatusEnum.FAIL.getCode() == inspectionStatus.get());
    }

    public void setInspectStatus(int mode, int inspectStatus){
        if (mode == InspectionModeEnum.VIDEO.getCode() || mode == InspectionModeEnum.ALL.getCode()) {
            this.inspectVideoStatus.set(inspectStatus);
        }

        if (mode == InspectionModeEnum.AUDIO.getCode() || mode == InspectionModeEnum.ALL.getCode()) {
            this.inspectAudioStatus.set(inspectStatus);
        }
    }

    public String getMediaSrvIp() {
        return mediaSrvIp;
    }

    public int getMediaSrvPort() {
        return mediaSrvPort;
    }

    public void setMediaSrvIp(String mediaSrvIp) {
        this.mediaSrvIp = mediaSrvIp;
    }

    public void setMediaSrvPort(int mediaSrvPort) {
        this.mediaSrvPort = mediaSrvPort;
    }

    public void addWaitMsg(String msgName, BaseRequestMsg msg){
        synchronized (this){
            if (null == waitMsg){
                waitMsg = new ConcurrentHashMap<>();
            }
        }

        waitMsg.put(msgName, msg);
    }

    public BaseRequestMsg getWaitMsg(String msgName){
        if (null == waitMsg)
            return null;

        return waitMsg.get(msgName);
    }

    public void delWaitMsg(String msgName){
        if (null == waitMsg)
            return;

        waitMsg.remove(msgName);
    }

    public ILocalConferenceParticipant getConferenceParticipant() {
        return conferenceParticipant;
    }

    public void setConferenceParticipant(ILocalConferenceParticipant conferenceParticipant) {
        this.conferenceParticipant = conferenceParticipant;
    }

    public void clearStatus(){
        this.inspectedStatus.set(InspectionStatusEnum.UNKNOWN.getCode());
        this.inspectionStatus.set(InspectionStatusEnum.UNKNOWN.getCode());
        this.inspectAudioStatus.set(InspectionStatusEnum.UNKNOWN.getCode());
        this.inspectVideoStatus.set(InspectionStatusEnum.UNKNOWN.getCode());
    }

    public void leftConference(){
        clearStatus();
        online.set(TerminalOnlineStatusEnum.OFFLINE.getCode());
        mtId = null;
        confId = null;
        groupId = null;
        conferenceParticipant.AllowAcceptExtensiveStreamRequest(false);
        toBeSpeaker.set(false);
        supportDualStream.set(false);
        inspectionParam = null;
        inspentedTerminals = null;
    }

    public abstract boolean onOpenLogicalChannel(Vector<MediaDescription> mediaDescriptions);

    public abstract void openDualStreamChannel(BaseRequestMsg startDualStreamRequest);

    public abstract boolean closeDualStreamChannel();

    public List<ExchangeInfo> getExchange(List<String> resourceInfo){
        if (null == resourceInfo || resourceInfo.isEmpty())
            return null;

        StringBuilder url = new StringBuilder();
        constructUrl(url, "/services/media/v1/exchange?GroupID={groupId}&Action=querynode");
        Map<String, String> args = new HashMap<>();
        args.put("groupId", groupId);

        QueryAndDelResourceParam queryResourceParam = new QueryAndDelResourceParam();
        queryResourceParam.setResourceIDs(resourceInfo);
        ResponseEntity<JSONObject> responseEntity = restClientService.exchangeJson(url.toString(), HttpMethod.POST, queryResourceParam, args, JSONObject.class);
        if (null == responseEntity) {
            System.out.println("getExchange, null == responseEntity, groupId:"+groupId);
            return null;
        }

        if (!responseEntity.getStatusCode().is2xxSuccessful()){
            System.out.println("getExchange, query resource info failed! status :"+responseEntity.getStatusCodeValue());
            return null;
        }

        JSONObject response = responseEntity.getBody();
        int code  = response.getInt("code");
        if (code != 0){
            String message=null;
            if (response.containsKey("message"))
                message = response.getString("message");

            System.out.println("getExchange, query resource info failed! url:"+url.toString()+", errcode :"+code+", errmsg:"+message);
            return null;
        }

        if (response.containsKey("exchangeNodeInfos")) {
            JSONArray exchangeNodeInfos = response.getJSONArray("exchangeNodeInfos");
            return JSONArray.toList(exchangeNodeInfos, new ExchangeInfo(), new JsonConfig());
        } else {
            System.out.println("getExchange, not find exchangeNodeInfos key in get exchange response message!");
            return null;
        }
    }

    public CreateResourceResponse addExchange(CreateResourceParam createResourceParam){
        //请求创建 /services/media/v1/exchange?GroupID={groupId}&Action=addnode
        StringBuilder url = new StringBuilder();
        constructUrl(url, "/services/media/v1/exchange?GroupID={groupId}&Action=addnode");
        Map<String, String> args = new HashMap<>();
        args.put("groupId", groupId);

        ResponseEntity<CreateResourceResponse> responseEntity = restClientService.exchangeJson(url.toString(), HttpMethod.POST, createResourceParam, args, CreateResourceResponse.class);
        if (null == responseEntity) {
            System.out.println("addExchange, null == responseEntity!");
            return null;
        }

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            System.out.println("create resource failed! status : " + responseEntity.getStatusCodeValue());
            return null;
        }

        CreateResourceResponse resourceResponse = responseEntity.getBody();
        if (resourceResponse.getCode() != 0) {
            System.out.println("create resource failed! errcode : " + resourceResponse.getCode()+", errmsg:"+resourceResponse.getMessage());
            return null;
        }

        return resourceResponse;
    }

    public boolean sendIFrame(){
        return conferenceParticipant.RequestKeyframe();
    }

    public void clearExchange(){
        List<DetailMediaResouce> forwardMediaResouces = forwardChannel;
        List<DetailMediaResouce> reverseMediaResouces = reverseChannel;

        if (null == forwardMediaResouces && null == reverseMediaResouces) {
            System.out.println("clearExchange, terminal("+getE164()+") has no resource! no need remove!");
            return;
        }

        List<String> resourceIds = new ArrayList<>();
        if (null != forwardMediaResouces) {
            for (DetailMediaResouce forwardResouce : forwardMediaResouces) {
                resourceIds.add(forwardResouce.getId());
            }
        }

        if (null != reverseMediaResouces) {
            for (DetailMediaResouce reverseResource : reverseMediaResouces) {
                resourceIds.add(reverseResource.getId());
            }
        }

        boolean bOk = removeExchange(resourceIds);
        if (bOk){
            System.out.println("clearExchange, OK, groupId:"+groupId);
        } else {
            System.out.println("clearExchange, fail, groupId:"+groupId);
        }

        System.out.println("clearExchange, start clean forwardChannel and reverseChannel!");
        if (null != forwardChannel) {
            System.out.println("clearExchange, forwardChannel is cleaned!");
            forwardChannel.clear();
            forwardChannel = null;
        }

        if (null != reverseChannel) {
            System.out.println("clearExchange, reverseChannel is cleaned!");
            reverseChannel.clear();
            reverseChannel = null;
        }
    }

    public boolean removeExchange(List<String> resourceIds){
        if (null == resourceIds || resourceIds.isEmpty())
            return true;

        QueryAndDelResourceParam removeParam = new QueryAndDelResourceParam();
        removeParam.setResourceIDs(resourceIds);

        StringBuilder url = new StringBuilder();
        constructUrl(url, "/services/media/v1/exchange?GroupID={groupId}&Action=removenode");
        Map<String, String> args = new HashMap<>();
        args.put("groupId", groupId);

        ResponseEntity<BaseResponseMsg> removeResponse = restClientService.exchangeJson(url.toString(), HttpMethod.POST, removeParam, args, BaseResponseMsg.class);
        if (null == removeResponse) {
            System.out.println("removeExchange, failed! null == removeResponse");
        } else if (!removeResponse.getStatusCode().is2xxSuccessful()){
            System.out.println("removeExchange, failed! status:"+removeResponse.getStatusCodeValue());
        } else if (removeResponse.getBody().getCode() != 0) {
            System.out.println("removeExchange, failed! errmsg:"+removeResponse.getBody().getMessage());
        } else {
            return true;
        }

        return false;
    }

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
                System.out.println("updateExchange, mediaDesc(mediaType:"+mediaDescription.getMediaType()+", streamIndex:"+mediaDescription.getStreamIndex()+")");
                System.out.println("                detailMediaResource(type:"+detailMediaResouce.getType()+", dual:"+detailMediaResouce.getDual()+")");

                if (!mediaDescription.getMediaType().equals(detailMediaResouce.getType()))
                    continue;

                if (mediaDescription.getStreamIndex() != detailMediaResouce.getStreamIndex())
                    continue;

                UpdateResourceParam updateResourceParam = new UpdateResourceParam(detailMediaResouce.getId());
                updateResourceParam.setSdp(constructSdp(mediaDescription));

                updateResourceParams.add(updateResourceParam);
                break;
            }
        }

       return requestUpdateResource(updateResourceParams);
    }

    protected boolean requestUpdateResource(List<UpdateResourceParam> updateResourceParams){
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

    protected String constructCreateSdp(MediaDescription mediaDescription){
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
    protected MediaDescription constructRequestMediaDescription(MediaDescription mediaDescription, String exchangeSdp){
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
        newMediaDescription.setStreamIndex(mediaDescription.getStreamIndex());
        newMediaDescription.setDual(mediaDescription.getDual());

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

    protected TransportAddress constructTransAddress(String sdp){
        if (null == sdp)
            return null;

        TransportAddress rtpAddress = new TransportAddress();

        boolean getAddress = false;
        boolean getPort = false;
        String[] splitResults = sdp.split("\r\n");
        for(String splitResult : splitResults){
            if (splitResult.contains("c=")){
                //获取地址
                System.out.println("constructTransAddress, "+splitResult);
                String[] addresses = splitResult.split(" ");
                rtpAddress.setIp(addresses[2]);
                getAddress = true;
            } else if (splitResult.contains("m=")){
                System.out.println("constructTransAddress, "+splitResult);
                String[] mediaInfos = splitResult.split(" ");
                rtpAddress.setPort(Integer.valueOf(mediaInfos[1]));
                getPort = true;
            }

            if (getAddress && getPort)
                break;
        }

        return rtpAddress;
    }

    protected String constructSdp(MediaDescription mediaDescription){
        if (null == mediaDescription)
            return "";

        StringBuilder sdp = new StringBuilder();
        sdp.append("c=IN IP4 ");
        sdp.append(mediaDescription.getRtpAddress().getIP());
        sdp.append("\r\n");

        if (mediaDescription.getMediaType().equals(MediaTypeEnum.VIDEO.getName())){
            VideoMediaDescription videoMediaDescription = (VideoMediaDescription)mediaDescription;
            sdp.append("m=video ");
            sdp.append(mediaDescription.getRtpAddress().getPort());
            sdp.append(" RTP/AVP ");
            sdp.append(mediaDescription.getPayload());
            sdp.append("\r\n");
            sdp.append("a=framerate:");
            sdp.append(videoMediaDescription.getFramerate());
            sdp.append("\r\n");
            sdp.append("a=rtpmap:");
            sdp.append(mediaDescription.getPayload());
            sdp.append(" ");
            sdp.append(mediaDescription.getEncodingFormat());
            sdp.append("/90000\r\n");

            if (mediaDescription.getEncodingFormat().equals(VideoMediaDescription.ENCODING_FORMAT_H264)){
                constructH264Fmtp(sdp, mediaDescription.getPayload(), videoMediaDescription.getH264Desc());
            }
        } else {
            AudioMediaDescription audioMediaDescription = (AudioMediaDescription)mediaDescription;
            sdp.append("m=audio ");
            sdp.append(mediaDescription.getRtpAddress().getPort());
            sdp.append(" RTP/AVP ");
            sdp.append(mediaDescription.getPayload());
            sdp.append("\r\n");
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

        if (mediaDescription.getDirection().equals(TransportDirectionEnum.SEND.getName())){
            sdp.append("a=sendonly\r\n");
        } else {
            sdp.append("a=recvonly\r\n");
        }

        return sdp.toString();
    }

    private int getH264LevelNum(int levelParamValue){
        if (levelParamValue == H264LevelParamEnum.LEVEL1.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL1.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL1B.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL1B.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL11.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL11.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL12.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL12.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL13.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL13.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL2.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL2.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL21.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL21.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL22.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL22.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL3.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL3.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL31.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL31.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL32.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL32.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL4.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL4.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL41.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL41.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL42.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL42.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL5.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL5.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL51.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL51.getH264LevelNum();
        } else if (levelParamValue == H264LevelParamEnum.LEVEL52.getLevelParamValue()){
            return H264LevelParamEnum.LEVEL52.getH264LevelNum();
        } else {
            return 0;
        }
    }

    private int getH264LevelParamValue(int levelNum){
        if (levelNum == H264LevelParamEnum.LEVEL1.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL1.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL1B.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL1B.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL11.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL11.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL12.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL12.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL13.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL13.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL2.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL2.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL21.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL21.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL22.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL22.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL3.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL3.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL31.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL31.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL32.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL32.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL4.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL4.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL41.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL41.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL42.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL42.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL5.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL5.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL51.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL51.getLevelParamValue();
        } else if (levelNum == H264LevelParamEnum.LEVEL52.getH264LevelNum()){
            return H264LevelParamEnum.LEVEL52.getLevelParamValue();
        } else {
            return 0;
        }
    }

    protected void constructH264Fmtp(StringBuilder sdp, int payload, H264Description h264Description){
        if (null == h264Description)
            return;

        String profile = h264Description.getProfile();
        int level = h264Description.getLevel();
        String  nalMode = h264Description.getNalMode();

        sdp.append("a=fmtp:");
        sdp.append(payload);
        sdp.append(" ");

        /*profile-level-id由三部分构成，profile_idc，profile_iop, level_idc*/
        sdp.append("profile-level-id=");
        switch (profile) {
            case H264Description.PROFILE_BASELINE:
                //66
                sdp.append("4200");
                break;
            case H264Description.PROFILE_MAIN:
                //77
                sdp.append("4D00");
                break;
            case H264Description.PROFILE_EXTEND:
                //88
                sdp.append("5800");
                break;
            case H264Description.PROFILE_HIGH:
                //100
                sdp.append("6400");
                break;
            default:
                sdp.append("0000");
                break;
        }

        Integer intLevel = getH264LevelNum(level);
        sdp.append(Integer.toHexString(intLevel));

        sdp.append(";packetization-mode=");
        switch (nalMode) {
            case H264Description.NAL_MODE_SINGLE:
                sdp.append("0");
                break;
            case H264Description.NAL_MODE_NOT_INTERLEAVED:
                sdp.append("1");
                break;
            case H264Description.NAL_MODE_INTERLEAVED:
                sdp.append("2");
                break;
            default:
                sdp.append("0");
                break;
        }

        sdp.append("\r\n");
    }

    protected void constructH264Desc(String profileLevelId, String packetizationMode, H264Description h264Description){
        if (profileLevelId.contains("42"))
            h264Description.setProfile(H264Description.PROFILE_BASELINE);
        else if (profileLevelId.contains("4D"))
            h264Description.setProfile(H264Description.PROFILE_MAIN);
        else if (profileLevelId.contains("58"))
            h264Description.setProfile(H264Description.PROFILE_EXTEND);
        else if (profileLevelId.contains("64"))
            h264Description.setProfile(H264Description.PROFILE_HIGH);
        else
            h264Description.setProfile(H264Description.PROFILE_BASELINE);

        String level = profileLevelId.substring(4);
        int levelNum = Integer.valueOf(level, 16);
        h264Description.setLevel(getH264LevelParamValue(levelNum));

        if (packetizationMode.equals("0"))
            h264Description.setNalMode(H264Description.NAL_MODE_SINGLE);
        else if (packetizationMode.equals("1"))
            h264Description.setNalMode(H264Description.NAL_MODE_NOT_INTERLEAVED);
        else if (packetizationMode.equals("2"))
            h264Description.setNalMode(H264Description.NAL_MODE_INTERLEAVED);
        else
            h264Description.setNalMode(H264Description.NAL_MODE_SINGLE);
    }

    protected void updateMediaResource(boolean bReverseChannel, List<ExchangeInfo> exchangeInfos){
        System.out.println("updateMediaResource, bReverseChannel:"+bReverseChannel+", exchangeInfoSize:"+exchangeInfos.size());
        CopyOnWriteArrayList<DetailMediaResouce> channel;
        if (bReverseChannel)
            channel = reverseChannel;
        else
            channel = forwardChannel;

        if (null == channel){
            System.out.println("updateMediaResource, channel == null!");
            return;
        }

        for(ExchangeInfo exchangeInfo : exchangeInfos){
            for (DetailMediaResouce detailMediaResouce : channel){
                if (!exchangeInfo.getResourceID().equals(detailMediaResouce.getId()))
                    continue;

                System.out.println("updateMediaResource, resourceId:"+exchangeInfo.getResourceID()+", localSdp:"+exchangeInfo.getLocalSdp());
                TransportAddress rtpAddress = constructTransAddress(exchangeInfo.getLocalSdp());
                TransportAddress rtcpAddress = new TransportAddress();
                rtcpAddress.setIp(rtpAddress.getIp());
                rtcpAddress.setPort(rtpAddress.getPort()+1);

                detailMediaResouce.setRtp(rtpAddress);
                detailMediaResouce.setRtcp(rtcpAddress);
                detailMediaResouce.setSdp(exchangeInfo.getLocalSdp());

                break;
            }
        }
    }

    protected void addMediaResource(int streamIndex, boolean dual, CreateResourceResponse resourceResponse){
        //添加媒体信息
        System.out.println("addMediaResource, resourceId:"+resourceResponse.getResourceID());
        System.out.println("exchangeSdp:");
        System.out.println(resourceResponse.getSdp());

        DetailMediaResouce detailMediaResouce = new DetailMediaResouce();
        detailMediaResouce.setStreamIndex(streamIndex);
        detailMediaResouce.setId(resourceResponse.getResourceID());
        detailMediaResouce.setDual(dual);

        if(resourceResponse.getSdp().contains("video")){
            detailMediaResouce.setType(MediaTypeEnum.VIDEO.getName());
        } else {
            detailMediaResouce.setType(MediaTypeEnum.AUDIO.getName());
        }

        TransportAddress rtpAddress = constructTransAddress(resourceResponse.getSdp());
        detailMediaResouce.setRtp(rtpAddress);

        TransportAddress rtcpAddress = new TransportAddress();
        rtcpAddress.setIp(rtpAddress.getIp());
        rtcpAddress.setPort(rtpAddress.getPort()+1);
        detailMediaResouce.setRtcp(rtcpAddress);
        detailMediaResouce.setSdp(resourceResponse.getSdp());

        if (resourceResponse.getSdp().contains("a=sendonly")) {
            addForwardChannel(detailMediaResouce);
        } else {
            addReverseChannel(detailMediaResouce);
        }
    }

    protected boolean removeMediaResource(boolean forwardResource, List<String> resourceIds){
        List<DetailMediaResouce> channel = null;

        if (forwardResource)
            channel = forwardChannel;
        else
            channel = reverseChannel;

        boolean bOk = removeExchange(resourceIds);
        for (String resourceId : resourceIds) {
            for (DetailMediaResouce detailMediaResouce : channel) {
                if (!detailMediaResouce.getId().equals(resourceId))
                    continue;
                channel.remove(detailMediaResouce);
                break;
            }
        }

        return bOk;
    }

    protected void parseRtpMapAndFmtp(String sdp, MediaDescription mediaDescription){
        if (null == sdp)
            return;

        String[] mediaSdps = sdp.split("\r\n");
        String fmtp = null;
        boolean getRtpMap = false;

        for (String mediaSdp : mediaSdps){
            if (mediaSdp.contains("a=fmtp:")) {
                fmtp = mediaSdp;
                if (getRtpMap)
                    break;
            }

            if (!mediaSdp.contains("a=rtpmap:"))
                continue;

            int startIndex = mediaSdp.indexOf(":");
            int endIndex = mediaSdp.indexOf(" ");
            String payload = mediaSdp.substring(startIndex+1, endIndex);
            mediaDescription.setPayload(Integer.valueOf(payload));

            startIndex = endIndex+1;
            endIndex = mediaSdp.indexOf("/");
            String encName = mediaSdp.substring(startIndex, endIndex);
            mediaDescription.setEncodingFormat(encName);
            getRtpMap = true;

            if (!encName.equals(VideoMediaDescription.ENCODING_FORMAT_H264))
                break;
        }

        if (null == fmtp)
            return;

        String profileLevelIdToken = "profile-level-id=";
        String packetizationModeToken = "packetization-mode=";
        int startIndex = fmtp.indexOf(profileLevelIdToken);
        int endIndex = fmtp.indexOf(";");
        startIndex+=profileLevelIdToken.length();
        String profileLevelId = fmtp.substring(startIndex, endIndex);

        startIndex = fmtp.indexOf(packetizationModeToken);
        startIndex+=packetizationModeToken.length();
        endIndex = startIndex+1;
        String packetizationMode = fmtp.substring(startIndex, endIndex);

        H264Description h264Description = new H264Description();
        constructH264Desc(profileLevelId, packetizationMode, h264Description);
        ((VideoMediaDescription)mediaDescription).setH264Desc(h264Description);
    }

    protected Vector<MediaDescription> constructAckMediaDescription(Vector<MediaDescription> mcuOpenChannelMediaDescriptions){
        if (null == mcuOpenChannelMediaDescriptions || mcuOpenChannelMediaDescriptions.isEmpty())
            return new Vector<>();

        CopyOnWriteArrayList<DetailMediaResouce> channels;
        Vector<MediaDescription> localMediaDescriptions = new Vector<>();

        //通过mcu主动打开通道时的参数，决定应答的参数方向
        //h323,mcu端只会主动打开mcu到终端的通道，即反向通道
        //sip，mcu端在打开mcu到终端的通道的同时，会携带终端到mcu方向的通道的接收地址，因此需要在回应时将正向和方向的地址全部带回
        for (MediaDescription mediaDescription : mcuOpenChannelMediaDescriptions) {
            if (TransportDirectionEnum.SEND.getName().equals(mediaDescription.getDirection()))
                channels = reverseChannel;
            else
                channels = forwardChannel;

            for (DetailMediaResouce detailMediaResouce : channels) {
                if (!mediaDescription.getMediaType().equals(detailMediaResouce.getType()))
                    continue;

                if (mediaDescription.getStreamIndex() != detailMediaResouce.getStreamIndex())
                    continue;

                MediaDescription localMediaDescription;
                if(detailMediaResouce.getType().equals(MediaTypeEnum.VIDEO.getName())){
                    VideoMediaDescription videoMediaDescription = new VideoMediaDescription();
                    try {
                        VideoMediaDescription mcuVideoDescription = (VideoMediaDescription)mediaDescription;
                        videoMediaDescription.setFramerate(mcuVideoDescription.getFramerate());
                        videoMediaDescription.setResolution(mcuVideoDescription.getResolution());
                        videoMediaDescription.setBitrateType(mcuVideoDescription.getBitrateType());

                    } catch (ClassCastException e){
                        videoMediaDescription.setFramerate(30);
                        videoMediaDescription.setResolution(VideoMediaDescription.RESOLUTION_1080P);
                        videoMediaDescription.setBitrateType(VideoMediaDescription.BITRATE_TYPE_CBR);
                    }

                    localMediaDescription = videoMediaDescription;
                } else {
                    AudioMediaDescription audioMediaDescription = new AudioMediaDescription();
                    try {
                        AudioMediaDescription mcuAudioDescription = (AudioMediaDescription)mediaDescription;
                        audioMediaDescription.setSampleRate(mcuAudioDescription.getSampleRate());
                        audioMediaDescription.setChannelNum(mcuAudioDescription.getChannelNum());
                    } catch (ClassCastException e){
                        audioMediaDescription.setSampleRate(8000);
                        audioMediaDescription.setChannelNum(1);
                    }

                    localMediaDescription = audioMediaDescription;
                }

                parseRtpMapAndFmtp(detailMediaResouce.getSdp(), localMediaDescription);

                localMediaDescription.setBitrate(mediaDescription.getBitrate());
                localMediaDescription.setMediaType(detailMediaResouce.getType());
                localMediaDescription.setStreamIndex(mediaDescription.getStreamIndex());
                localMediaDescription.setDual(mediaDescription.getDual());

                if (TransportDirectionEnum.SEND.getName().equals(mediaDescription.getDirection())){
                    localMediaDescription.setDirection(TransportDirectionEnum.RECV.getName());
                } else {
                    localMediaDescription.setDirection(TransportDirectionEnum.SEND.getName());
                }

                TransportAddress rtpTransAddress = detailMediaResouce.getRtp();
                NetAddress rtpAddress = new NetAddress();
                rtpAddress.setIP(rtpTransAddress.getIp());
                rtpAddress.setPort(rtpTransAddress.getPort());
                localMediaDescription.setRtpAddress(rtpAddress);

                TransportAddress rtcpTransAddress = detailMediaResouce.getRtcp();
                NetAddress rtcpAddress = new NetAddress();
                rtcpAddress.setIP(rtcpTransAddress.getIp());
                rtcpAddress.setPort(rtcpTransAddress.getPort());
                localMediaDescription.setRtcpAddress(rtcpAddress);

                localMediaDescriptions.add(localMediaDescription);
				break;
            }
        }

        return localMediaDescriptions;
    }

    protected boolean ackOpenLogicalChannel(Vector<MediaDescription> mcuDescriptions){
        Vector<MediaDescription> localMediaDescriptions = constructAckMediaDescription(mcuDescriptions);
        boolean bOk = conferenceParticipant.ResponseLocalMediaToRemotePeer(localMediaDescriptions);
        if (!bOk) {
            System.out.println("ResponseLocalMediaToRemotePeer failed! participartId : " + e164);
            return false;
        }
        System.out.println("ResponseLocalMediaToRemotePeer OK! participartId : " + e164);
        return true;
    }

    protected void constructUrl(StringBuilder url, String restApi){
        if (url.length() > 0) {
            url.delete(0, url.length());
        }

        url.append("http://");
        url.append(mediaSrvIp);
        url.append(":");
        url.append(mediaSrvPort);
        url.append(restApi);
    }

    protected String groupId;
    protected String e164;
    protected String name;
    protected String confId;
    protected String mtId;
    protected String occupyConfName;
    protected int type;  //1:会议终端，2:虚拟终端
    protected InspectionSrcParam inspectionParam;
    protected ConcurrentHashMap<String, InspectedParam> inspentedTerminals; //<选看目的E164号,被选看参数>
    protected AtomicInteger online;
    protected AtomicInteger inspectionStatus;   //0-未知,1-选看成功，2-选看失败
    protected AtomicInteger inspectedStatus;    //0-未知,1-选看成功，2-选看失败
    protected AtomicBoolean toBeSpeaker;
    protected AtomicBoolean supportDualStream;   //0-不支持双流,1-支持双流
    protected AtomicInteger inspectVideoStatus;   //0-未知,1-选看成功，2-选看失败
    protected AtomicInteger inspectAudioStatus;
    protected CopyOnWriteArrayList<DetailMediaResouce> forwardChannel;
    protected CopyOnWriteArrayList<DetailMediaResouce> reverseChannel;
    protected ILocalConferenceParticipant conferenceParticipant;
    protected String mediaSrvIp;
    protected int mediaSrvPort;

    protected ConcurrentHashMap<String, BaseRequestMsg> waitMsg;

    protected final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected RestClientService restClientService;
}
