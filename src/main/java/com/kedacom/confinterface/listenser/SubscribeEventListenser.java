package com.kedacom.confinterface.listenser;

import com.kedacom.confinterface.dao.BroadcastSrcMediaInfo;
import com.kedacom.confinterface.dao.BroadcastTypeEnum;
import com.kedacom.confinterface.dao.InspectionSrcParam;
import com.kedacom.confinterface.dto.*;
import com.kedacom.confinterface.event.SubscribeEvent;
import com.kedacom.confinterface.inner.*;
import com.kedacom.confinterface.restclient.McuRestClientService;
import com.kedacom.confinterface.restclient.mcu.*;
import com.kedacom.confinterface.service.ConfInterfacePublishService;
import com.kedacom.confinterface.service.ConfInterfaceService;
import com.kedacom.confinterface.service.TerminalMediaSourceService;
import com.kedacom.confinterface.service.TerminalService;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class SubscribeEventListenser implements ApplicationListener<SubscribeEvent> {
    @Async("confTaskExecutor")
    @Override
    public void onApplicationEvent(SubscribeEvent subscribeEvent) {
        System.out.println("onApplicationEvent, recv subscribe event, confId:"+subscribeEvent.getConfId()+", channel:"+subscribeEvent.getChannel()+", threadName:"+Thread.currentThread().getName());
        String groupId = confInterfaceService.getGroupId(subscribeEvent.getConfId());
        if (null == groupId){
            System.out.println("not find groupId, confId : " + subscribeEvent.getConfId());
            return;
        }

        GroupConfInfo groupConfInfo = confInterfaceService.getGroupConfInfo(groupId);
        if (null == groupConfInfo){
            System.out.println("not find groupConfInfo, groupId : " + groupId);
            return;
        }

        BaseRequestMsg<? extends BaseResponseMsg> requestMsg = groupConfInfo.getWaitDealTask(subscribeEvent.getChannel());
        if (null == requestMsg) {
            //非外部请求的消息，根据通道判断是什么内容的更新删除
            System.out.println("no requestMsg..........");
            processSubscribeMsg(subscribeEvent, groupConfInfo);
            return;
        }

        if (requestMsg instanceof BroadCastRequest) {
            System.out.println("process BroadCastRequest.......");
            BroadCastRequest broadCastRequest = (BroadCastRequest) requestMsg;
            processBroadcastRequest(subscribeEvent, groupConfInfo, broadCastRequest);
        } else if (requestMsg instanceof JoinDiscussionGroupRequest) {
            System.out.println("process JoinDiscussionGroupRequest.........");
            JoinDiscussionGroupRequest joinDiscussionGroupRequest = (JoinDiscussionGroupRequest) requestMsg;
            processJoinDiscussionRequest(subscribeEvent, groupConfInfo, joinDiscussionGroupRequest);
        } else if (requestMsg instanceof LeftDiscussionGroupRequest) {
            System.out.println("process LeftDiscussionGroupRequest..........");
            LeftDiscussionGroupRequest leftDiscussionGroupRequest = (LeftDiscussionGroupRequest) requestMsg;
            processLeftDiscussionRequest(subscribeEvent, groupConfInfo, leftDiscussionGroupRequest);
        } else if (requestMsg instanceof InspectionRequest) {
            System.out.println("process InspectionRequest..........");
            InspectionRequest inspectionRequest = (InspectionRequest) requestMsg;
            processInspectionRequest(subscribeEvent, groupConfInfo, inspectionRequest);
        } else if (requestMsg instanceof CancelInspectionRequest) {
            System.out.println("process CancelInspectionRequest..........");
            CancelInspectionRequest cancelInspectionRequest = (CancelInspectionRequest)requestMsg;
            processCancelInspectionRequest(subscribeEvent, groupConfInfo, cancelInspectionRequest);
        } else if (requestMsg instanceof CancelDualStreamRequest){
            System.out.println("process CancelDualStreamRequest..........");
            CancelDualStreamRequest cancelDualStreamRequest = (CancelDualStreamRequest) requestMsg;
            processCancelDualStreamRequest(subscribeEvent, groupConfInfo, cancelDualStreamRequest);
        } else if (requestMsg instanceof StartDualStreamRequest){
            System.out.println("process StartDualStreamRequest..........");
            StartDualStreamRequest startDualStreamRequest = (StartDualStreamRequest) requestMsg;
            processStartDualStreamRequest(subscribeEvent, groupConfInfo, startDualStreamRequest);
        }
    }

    private void resetBroadcastSrc(GroupConfInfo groupConfInfo, String broadcastMtId){
        String confId = groupConfInfo.getConfId();
        int broadcastType = groupConfInfo.getBroadcastType();
        if (broadcastType == BroadcastTypeEnum.OTHER.getCode()){
            //如果广播源不是会议终端，则该广播源为虚拟终端，直接设置虚拟终端为发言人
            System.out.println("vmt, setSpeaker, confId : "+confId+", mtId: " + broadcastMtId);
            mcuRestClientService.setSpeaker(confId, broadcastMtId);
            return;
        }

        if (broadcastType == BroadcastTypeEnum.TERMINAL.getCode()) {
            //如果广播源为终端，则直接设置该终端为发言人
            String broadcastE164 = groupConfInfo.getBroadcastMtE164();
            TerminalService mtService = groupConfInfo.getMtMember(broadcastE164);
            if (mtService.isOnline()){
                System.out.println("terminal, setSpeaker, confId : "+confId+", e164: "+mtService.getE164()+", mtId: "+mtService.getMtId());
                mcuRestClientService.setSpeaker(confId, mtService.getMtId());
            } else {
                //等到设备上线后,再设置为发言人
                System.out.println("terminal, not online, set to be speaker, confId : "+confId+", e164: "+mtService.getE164()+", mtId: "+mtService.getMtId());
                mtService.setToBeSpeaker(true);
            }
        }
    }

    private void tryInspections(String confId, String mode, String srcMtId, String dstMtId){
        int tryTimes = 3;
        do {
            McuStatus mcuStatus = mcuRestClientService.inspections(confId, mode, srcMtId, dstMtId);
            if (mcuStatus.getValue() == 0 || tryTimes == 1){
                //选看成功或者尝试次数到了，直接退出循环
                System.out.println("tryInspections, confId:"+confId+", mode:"+mode+", srcMtId:"+srcMtId+", dstMtId:"+dstMtId+", errCode:"+mcuStatus.getValue()+", tryTimes:"+tryTimes);
                break;
            }

            //如果选看失败，此处暂时睡眠1s，保证源和目的的逻辑通道已经打开
            //虽然在终端上线时，已经判断了终端信息中的通道是否存在，但是会议那边的反馈是
            //查询到通道信息也不代表逻辑通道已经打开，因此，只能采用延时策略，否则选看可能会失败
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e){
            }
        } while (--tryTimes > 0);
    }

    private void resumeInspection(GroupConfInfo groupConfInfo, TerminalService onlineTerminal){
        String confId = groupConfInfo.getConfId();
        InspectionSrcParam inspectionSrcParam = onlineTerminal.getInspectionParam();
        if (null != inspectionSrcParam && !onlineTerminal.isInspection()) {
            //处理选看
            String inspectionSrcE164 = inspectionSrcParam.getMtE164();
            String mode = inspectionSrcParam.getMode();
            TerminalService inspectionSrcService = groupConfInfo.getMember(inspectionSrcE164);
            if (inspectionSrcService.isOnline()){
                System.out.println("src[" + inspectionSrcE164 + "] is online, and logical channel has been opened, " + "dst[" + onlineTerminal.getE164() + "] start inspection!");
                tryInspections(confId, mode, inspectionSrcService.getMtId(), onlineTerminal.getMtId());
            } else {
                System.out.println("src["+inspectionSrcE164+"] is not online, set be inspected by dst["+onlineTerminal.getE164()+"]");
                InspectedParam inspectedParam = new InspectedParam();
                inspectedParam.setVmt(onlineTerminal.isVmt());
                inspectionSrcService.addInspentedTerminal(onlineTerminal.getE164(), inspectedParam);
            }
        }

        //是否被选看
        System.out.println("check terminal["+onlineTerminal.getE164()+"] whether be inspected!");
        ConcurrentHashMap<String, InspectedParam> inspectedTerminals = onlineTerminal.getInspentedTerminals();
        if (null == inspectedTerminals) {
            //没有被选看，直接返回
            System.out.println("terminal["+onlineTerminal.getE164()+"] not be inspected!");
            return;
        }

        System.out.println("terminal["+onlineTerminal.getE164()+"] start dealing be inspected! total "+inspectedTerminals.size()+" terminal inspect this termianl!");
        TerminalService dstInspectionService;
        for (ConcurrentHashMap.Entry<String, InspectedParam> inspectedTerminal : inspectedTerminals.entrySet()){
            dstInspectionService = groupConfInfo.getMember(inspectedTerminal.getKey());
            if (null == dstInspectionService)
                continue;

            if(!dstInspectionService.isOnline()) {
                System.out.println("dstTerminal["+dstInspectionService.getE164()+"] is not online, processing inspection when dst is online!");
                continue;
            }

            if(inspectedTerminal.getValue().getStatus() == InspectionStatusEnum.OK){
                System.out.println("dstTerminal["+dstInspectionService.getE164()+"] has inspected srcTerminal["+onlineTerminal.getE164()+"]");
                continue;
            }

            inspectionSrcParam = dstInspectionService.getInspectionParam();
            String mode = inspectionSrcParam.getMode();
            System.out.println("dst[" + dstInspectionService.getE164() + "] is online, and logical channel has been opened, start inspect src[" + onlineTerminal.getE164() + "]");
            tryInspections(confId, mode, onlineTerminal.getMtId(), dstInspectionService.getMtId());
        }
    }

    private void processSubscribeMts(GroupConfInfo groupConfInfo, String channel){
        //通道为 /confs/{conf_id}/cascades/{cascade_id}/mts/{mt_id}
        String[] parseResult = channel.split("/");
        String confId = parseResult[2];
        String mtId = parseResult[6];

        //todo：此处暂时未考虑mcu级联的情况，后面根据需要，可以考虑
        GetConfMtInfoResponse getConfMtInfoResponse = mcuRestClientService.getConfMtInfo(confId, mtId);
        if (null == getConfMtInfoResponse)
            return;

        System.out.println("processSubscribeMts, ConfMtInfo:"+getConfMtInfoResponse.toString());
        String e164 = getConfMtInfoResponse.getE164();
        TerminalService terminalService = groupConfInfo.getMember(e164);
        if (null == terminalService){
            return;
        }

        System.out.println("processSubscribeMts, mtId:"+mtId+", e164:"+e164+", terminalService mtId:"+terminalService.getMtId());

        if (terminalService.isOnline()){
            System.out.println("processSubscribeMts, terminal(e164:"+terminalService.getE164()+",mtId:"+mtId+") is current online!");
            if (getConfMtInfoResponse.getOnline() == 0){
                if (!terminalService.isVmt()) {
                    //只上报会议终端的状态
                    TerminalStatusNotify terminalStatusNotify = new TerminalStatusNotify();
                    TerminalStatus terminalStatus = new TerminalStatus(e164, 0);
                    terminalStatusNotify.addMtStatus(terminalStatus);
                    confInterfacePublishService.publishMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS, groupConfInfo.getGroupId(), terminalStatusNotify);
                }

                terminalService.setOnline(TerminalOnlineStatusEnum.OFFLINE.getCode());
                terminalService.clearStatus();

                System.out.println("terminal(e164:"+e164+",mtId:"+mtId+") is offline! confId:"+groupConfInfo.getConfId()+", vmt:"+terminalService.isVmt());
                //判断是否有其他的终端选看该终端，如果有，则重置相应终端的选看状态
                if (!terminalService.isInspected())
                    return;

                ConcurrentHashMap<String, InspectedParam> inspentedTerminals = terminalService.getInspentedTerminals();
                for (ConcurrentHashMap.Entry<String, InspectedParam> inspectedTerminal : inspentedTerminals.entrySet()){
                    TerminalService inspectionService = groupConfInfo.getMember(inspectedTerminal.getKey());
                    inspectionService.setInspectionStatus(InspectionStatusEnum.UNKNOWN);
                    inspectionService.setInspectAudioStatus(InspectionStatusEnum.UNKNOWN.getCode());
                    inspectionService.setInspectVideoStatus(InspectionStatusEnum.UNKNOWN.getCode());
                }

                return;
            }
        } else {
            if (getConfMtInfoResponse.getOnline() == 1) {
                if (null == groupConfInfo.getE164(mtId)) {
                    terminalService.setMtId(mtId);
                    terminalService.setConfId(groupConfInfo.getConfId());
                    groupConfInfo.addMtId(mtId, terminalService.getE164());
                }

                //等到逻辑通道成功打开再设置为终端上线，在逻辑通道没有打开之前，进行选看等其他操作会失败
                if (getConfMtInfoResponse.getType() == 3/*电话终端*/ || (getConfMtInfoResponse.getType() == 1 && getConfMtInfoResponse.getBitrate() == 64)/*音频终端*/){
                    //只会打开音频逻辑通道
                    if(!getConfMtInfoResponse.getA_snd_chn().isEmpty() && !getConfMtInfoResponse.getA_rcv_chn().isEmpty()){
                        terminalService.setOnline(TerminalOnlineStatusEnum.ONLINE.getCode());
                        System.out.println("telephone terminal(E164:" + e164 + ",mtId:" + mtId + ") is online! confId:" + groupConfInfo.getConfId() + ", vmt:" + terminalService.isVmt());
                    }
                } else {
                    if (!getConfMtInfoResponse.getA_snd_chn().isEmpty() && !getConfMtInfoResponse.getA_rcv_chn().isEmpty()
                            && !getConfMtInfoResponse.getV_snd_chn().isEmpty() && !getConfMtInfoResponse.getV_rcv_chn().isEmpty()){
                        terminalService.setOnline(TerminalOnlineStatusEnum.ONLINE.getCode());
                        System.out.println("terminal(E164:" + e164 + ",mtId:" + mtId + ") is online! confId:" + groupConfInfo.getConfId() + ", vmt:" + terminalService.isVmt()+", type:"+getConfMtInfoResponse.getType());
                    }
                }

                if (terminalService.isOnline() && !terminalService.isVmt()) {
                    TerminalStatusNotify terminalStatusNotify = new TerminalStatusNotify();
                    TerminalStatus terminalStatus = new TerminalStatus(e164, 1);
                    terminalStatusNotify.addMtStatus(terminalStatus);
                    confInterfacePublishService.publishMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS, groupConfInfo.getGroupId(), terminalStatusNotify);
                }
            }

            if (terminalService.isOnline()){
                //会议会自动恢复发言人，因此此处不再恢复发言人，但是需要恢复选看
                //是否需要恢复选看
                resumeInspection(groupConfInfo, terminalService);
            }
        }
    }

    private void processSubscribeMtsNotify(SubscribeEvent subscribeEvent, GroupConfInfo groupConfInfo){
        //添加失败
        System.out.println("now in processSubscribeMtsNotify......");
        JoinConfFailInfo joinConfFailInfo = (JoinConfFailInfo) subscribeEvent.getContent();
        String failE164 = joinConfFailInfo.getJoinConfFailMt().getAccount();
        System.out.println("terminal (e164: " + failE164 + " ) join conference failed, error_code : " + subscribeEvent.getErrorCode());

        int status = 255;
        TerminalService terminalService = groupConfInfo.getMember(failE164);
        if (20423 == subscribeEvent.getErrorCode()){
            //返回该错误码时，表明终端被另外一个会议占用，因此入会失败
            terminalService.setOnline(TerminalOnlineStatusEnum.OCCUPIED.getCode());
            terminalService.setOccupyConfName(joinConfFailInfo.getOccupy_confname());
            System.out.println("occupy conf name : " + terminalService.getOccupyConfName());
            status = 2;
        } else {
            terminalService.setOnline(TerminalOnlineStatusEnum.OFFLINE.getCode());
        }

        if (!terminalService.isVmt()) {
            TerminalStatusNotify terminalStatusNotify = new TerminalStatusNotify();
            TerminalStatus terminalStatus = new TerminalStatus(terminalService.getE164(), status);
            terminalStatusNotify.addMtStatus(terminalStatus);
            confInterfacePublishService.publishMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS, groupConfInfo.getGroupId(), terminalStatusNotify);
        }
    }

    private void processSubscribeMsg(SubscribeEvent subscribeEvent, GroupConfInfo groupConfInfo){
        //判断是什么通道
        String channel = subscribeEvent.getChannel();
        String method = subscribeEvent.getMethod();
        System.out.println("processSubscribeMsg, method : " + method + ", channel : " + channel);

        if (channel.contains("mts")){
            //终端列表 /confs/{conf_id}/cascades/{cascade_id}/mts/{mt_id}
            //添加终端失败通知 /confs/{conf_id}/mts
            if (channel.contains("cascades")){
                //todo:通道列表,获取通道列表信息，并推送给调度服务
                if (method.equals(SubscribeMethodEnum.DELETE.getName())) {
                    return;
                }
                processSubscribeMts(groupConfInfo, channel);
            } else {
                processSubscribeMtsNotify(subscribeEvent, groupConfInfo);
            }
        } else if (channel.contains("inspections")){
            if (method.equals(SubscribeMethodEnum.DELETE.getName())){
                processCancelInspection(subscribeEvent, groupConfInfo);
            } else if (method.equals(SubscribeMethodEnum.UPDATE.getName())){
                processInspectionOK(subscribeEvent, groupConfInfo, false, null);
            } else if (method.equals(SubscribeMethodEnum.NOTIFY.getName())){
                processInspectionFail(subscribeEvent, groupConfInfo, null);
            }
        } else if (channel.contains("speaker")){
            //发言人相关通道
            System.out.println("get speaker subscribe message, channel :" + channel);
        }
    }

    private void processBroadcastRequest(SubscribeEvent subscribeEvent, GroupConfInfo groupConfInfo, BroadCastRequest broadCastRequest) {
        //如果是广播请求，则执行获取发言人以校验是否设置成功
        BroadcastSrcMediaInfo broadcastSrcMediaInfo = new BroadcastSrcMediaInfo();
        TerminalService broadcastTerminalService = groupConfInfo.getBroadcastVmtService();
        System.out.println("processBroadcastRequest, groupId:" + broadcastTerminalService.getGroupId() + ", broadcast mtId:" + broadcastTerminalService.getMtId());

        boolean isTerminal = broadCastRequest.getBroadCastParam().isTerminalType();
        String broadcastMtId = broadcastTerminalService.getMtId();
        System.out.println("processBroadcastRequest, isTerminal : " + isTerminal +", channel:"+subscribeEvent.getChannel());
        if (isTerminal) {
            String speaker = broadCastRequest.getBroadCastParam().getMtE164();
            broadcastSrcMediaInfo.setType(BroadcastTypeEnum.TERMINAL.getCode());
            broadcastSrcMediaInfo.setMtE164(speaker);
            TerminalService terminalService = groupConfInfo.getMtMember(speaker);
            broadcastMtId = terminalService.getMtId();
        } else {
            broadcastSrcMediaInfo.setType(BroadcastTypeEnum.OTHER.getCode());
            broadcastSrcMediaInfo.setMtE164(null);
        }

        broadcastSrcMediaInfo.setVmtE164(broadcastTerminalService.getE164());
        String speaker = mcuRestClientService.getSpeaker(groupConfInfo.getConfId());
        if (null != speaker && speaker.equals(broadcastMtId)) {
            terminalMediaSourceService.setBroadcastSrcInfo(groupConfInfo.getGroupId(), broadcastSrcMediaInfo);
            broadCastRequest.setForwardResources(TerminalMediaResource.convertToMediaResource(broadcastTerminalService.getForwardChannel(), "all"));
            broadCastRequest.setReverseResources(TerminalMediaResource.convertToMediaResource(broadcastTerminalService.getReverseChannel(), "all"));
            broadCastRequest.makeSuccessResponseMsg();
        } else {
            broadCastRequest.makeErrorResponseMsg(ConfInterfaceResult.SET_SPEAKER.getCode(), HttpStatus.OK, ConfInterfaceResult.SET_SPEAKER.getMessage());
        }

        broadCastRequest.removeMsg(subscribeEvent.getChannel());
        groupConfInfo.delWaitDealTask(subscribeEvent.getChannel());
    }

    private void processDiscussionFail(JoinDiscussionGroupRequest joinDiscussionGroupRequest){
        System.out.println("processDiscussionFail................");
        if (joinDiscussionGroupRequest.getWaitMsg().isEmpty()){
            System.out.println("processDiscussionFail, all messages have been dealed!");
            joinDiscussionGroupRequest.makeErrorResponseMsg(ConfInterfaceResult.INSPECTION.getCode(), HttpStatus.OK, ConfInterfaceResult.INSPECTION.getMessage());
        }
    }

    private void processDiscussionOk(JoinDiscussionGroupRequest joinDiscussionGroupRequest, GroupConfInfo groupConfInfo, TerminalService dstVmtTerminal, TerminalService mtTerminal){
        System.out.println("processDiscussionOk.................");
        String mtE164 = mtTerminal.getE164();
        TerminalService inspectedByMtVmtService = groupConfInfo.getSrcInspectionTerminal(mtTerminal);
        List<DetailMediaResouce> forwardDetailResource = inspectedByMtVmtService.getForwardChannel();
        List<DetailMediaResouce> reverseDetailResource = dstVmtTerminal.getReverseChannel();

        TerminalMediaResource terminalMediaResource = new TerminalMediaResource();
        terminalMediaResource.setMtE164(mtE164);
        terminalMediaResource.setForwardResources(TerminalMediaResource.convertToMediaResource(forwardDetailResource, "all"));
        terminalMediaResource.setReverseResources(TerminalMediaResource.convertToMediaResource(reverseDetailResource, "all"));
        joinDiscussionGroupRequest.addTerminalMediaResource(terminalMediaResource);
    }

    private void processInspectionFailStatus(InspectionModeEnum inspectionFailMode, String confId, TerminalService srcTerminal, TerminalService dstTerminal){
        String dstInspectMode = dstTerminal.getInspectionParam().getMode();

        dstTerminal.setInspectStatus(inspectionFailMode.getCode(), InspectionStatusEnum.FAIL.getCode());
        if (dstInspectMode.equals(inspectionFailMode.getName())) {
            dstTerminal.setInspectionStatus(InspectionStatusEnum.FAIL);
        } else if (dstInspectMode.equals(InspectionModeEnum.ALL.getName())) {
            InspectionModeEnum inspectionVideoOrAudioMode;
            if (inspectionFailMode == InspectionModeEnum.VIDEO)
                inspectionVideoOrAudioMode = InspectionModeEnum.AUDIO;
            else
                inspectionVideoOrAudioMode = InspectionModeEnum.VIDEO;

            if (dstTerminal.getInspectStatus(inspectionVideoOrAudioMode.getCode()) == InspectionStatusEnum.OK.getCode()) {
                System.out.println("terminal["+dstTerminal.getE164()+"] "+inspectionFailMode.getName()+" inspection failed, "+inspectionVideoOrAudioMode.getName()+" inspection ok, cancel "+inspectionVideoOrAudioMode.getName()+" inspection!");
                mcuRestClientService.cancelInspection(confId, inspectionVideoOrAudioMode.getName(), dstTerminal.getMtId());
            } else if (dstTerminal.getInspectStatus(inspectionVideoOrAudioMode.getCode()) == InspectionStatusEnum.FAIL.getCode()) {
                /*设置该终端不在选看*/
                dstTerminal.setInspectionStatus(InspectionStatusEnum.FAIL);
            }
        }

        if (dstTerminal.isInspectionFail()) {
            dstTerminal.setInspectionParam(null);
            srcTerminal.delInspentedTerminal(dstTerminal.getE164());
        }
    }

    private void processInspectionFail(SubscribeEvent subscribeEvent, GroupConfInfo groupConfInfo, BaseRequestMsg<? extends BaseResponseMsg> requestMsg) {
        /*选看失败 通道中的mt_id是选看目的终端号*/
        //设置相应终端的相应模式选看失败
        String[] parseResult = subscribeEvent.getChannel().split("/");
        int mode = Integer.valueOf(parseResult[5]);
        String dstMtId = parseResult[4];
        TerminalService dstTerminal = groupConfInfo.getDstInspectionTerminal(dstMtId);
        TerminalService srcTerminal = groupConfInfo.getSrcInspectionTerminal(dstTerminal);

        System.out.println("processInspectionFail, terminal["+dstTerminal.getE164()+"] inspect terminal["+srcTerminal.getE164()+"] failed, mode:"+mode);

        String confId = groupConfInfo.getConfId();
        if (mode == InspectionModeEnum.VIDEO.getCode()) {
            processInspectionFailStatus(InspectionModeEnum.VIDEO, confId, srcTerminal, dstTerminal);
        } else if (mode == InspectionModeEnum.AUDIO.getCode()){
            processInspectionFailStatus(InspectionModeEnum.AUDIO, confId, srcTerminal, dstTerminal);
        } else {
            return;
        }

        //如果源已经选看目的成功，则需要进行取消选看
        int srcInspectAudioStatus = InspectionStatusEnum.UNKNOWN.getCode();
        int srcInspectVideoStatus = InspectionStatusEnum.UNKNOWN.getCode();
        if (requestMsg instanceof JoinDiscussionGroupRequest) {
            //如果加入讨论组操作中的选看失败,需要处理源的选看
            TerminalService cancelDstService;
            boolean isStopInspection = true;
            if (!srcTerminal.isVmt()) {
                //如果srcTerminal为会议终端，则dstTerminal为虚拟终端，此时需要取消的就是该会议终端的选看
                cancelDstService = srcTerminal;
            } else {
                //srcTerminal为虚拟终端，则dstTerminal为会议终端，此时需要获取选看该会议终端的虚拟终端，并进行取消选看
                cancelDstService = groupConfInfo.getDstInspectionVmtTerminal(dstTerminal);
                isStopInspection = ((JoinDiscussionGroupRequest) requestMsg).isStopInspection(dstTerminal.getE164());
                System.out.println("processInspectionFail, bDiscussion, isStopInspection:"+isStopInspection+", mtE164:"+dstTerminal.getE164());
            }

            srcInspectAudioStatus = cancelDstService.getInspectAudioStatus();
            srcInspectVideoStatus = cancelDstService.getInspectVideoStatus();

            if (isStopInspection) {
                String cancelInspectionDstMtId = cancelDstService.getMtId();
                if (srcInspectAudioStatus == InspectionStatusEnum.OK.getCode()) {
                    System.out.println("processInspectionFail, terminal[" + cancelDstService.getE164() + "] inspect terminal[" + cancelDstService.getInspectionParam().getMtE164() + "] audio ok, need cancel inspection!");
                    mcuRestClientService.cancelInspection(confId, InspectionModeEnum.AUDIO.getName(), cancelInspectionDstMtId);
                }

                if (srcInspectVideoStatus == InspectionStatusEnum.OK.getCode()) {
                    System.out.println("processInspectionFail, terminal[" + cancelDstService.getE164() + "] inspect terminal[" + cancelDstService.getInspectionParam().getMtE164() + "] video ok, need cancel inspection!");
                    mcuRestClientService.cancelInspection(confId, InspectionModeEnum.VIDEO.getName(), cancelInspectionDstMtId);
                }
            }
        }

        if (dstTerminal.isVmt()) {
            if (dstTerminal.isInspectionFail() && (dstTerminal.getInspentedTerminals() == null || dstTerminal.getInspentedTerminals().isEmpty())) {
                groupConfInfo.freeVmt(dstTerminal.getE164());
            }
        } else if (srcTerminal.isVmt()){
            if (srcTerminal.isInspectionFail() && (srcTerminal.getInspentedTerminals() == null || srcTerminal.getInspentedTerminals().isEmpty())){
                groupConfInfo.freeVmt(srcTerminal.getE164());
            }
        }

        if (null == requestMsg)
            return;

        //等到相互选看的两个终端的订阅消息全部收到后，再处理
        //其他三个方向的订阅消息都已经收到，不管成功还是失败，在此回应失败
        //如果其他三个方向的订阅消息还有未收到的，等到收到后再处理
        int unknownStatus = InspectionStatusEnum.UNKNOWN.getCode();
        if (requestMsg instanceof JoinDiscussionGroupRequest) {
            System.out.println("processInspectionFail, start deal discussionGroupRequest, dstInspectAudioStatus:"+dstTerminal.getInspectAudioStatus()+", dstInspectVideoStatus:"+dstTerminal.getInspectVideoStatus()+
                                ", srcInspectAudioStatus:"+srcInspectAudioStatus+", srcInspectVideoStatus:"+srcInspectVideoStatus);

            JoinDiscussionGroupRequest joinDiscussionGroupRequest = (JoinDiscussionGroupRequest) requestMsg;
            if (dstTerminal.getInspectAudioStatus() != unknownStatus && dstTerminal.getInspectVideoStatus() != unknownStatus
                    && srcInspectAudioStatus != unknownStatus && srcInspectVideoStatus != unknownStatus) {
                processDiscussionFail(joinDiscussionGroupRequest);
            }
        } else if (requestMsg instanceof InspectionRequest) {
            System.out.println("processInspectionFail, start deal inspectionRequest!!!");
            InspectionRequest inspectionRequest = (InspectionRequest) requestMsg;
            inspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.INSPECTION.getCode(), HttpStatus.OK, ConfInterfaceResult.INSPECTION.getMessage());
        }
    }

    private void processInspectionOkStatus(InspectionModeEnum inspectionOkMode, GroupConfInfo groupConfInfo, boolean bDiscussion, TerminalService srcTerminal, TerminalService dstTerminal){
        //视频
        String dstInspectMode = dstTerminal.getInspectionParam().getMode();
        System.out.println("processInspectionOkStatus, InspectOkMode:"+inspectionOkMode.getName()+", InspectionMode:"+dstInspectMode);
        dstTerminal.setInspectStatus(inspectionOkMode.getCode(), InspectionStatusEnum.OK.getCode());
        if (dstInspectMode.equals(inspectionOkMode.getName())) {
            dstTerminal.setInspectionStatus(InspectionStatusEnum.OK);
        } else if (dstInspectMode.equals(InspectionModeEnum.ALL.getName())) {
            boolean cancelInspection = false;
            if (bDiscussion) {
                TerminalService inspectMtVmtService;
                TerminalService mtService;
                if (srcTerminal.isVmt()){
                    //如果源是虚拟终端，则dstTermial为会议终端，获取选看该会议终端的虚拟终端
                    inspectMtVmtService = groupConfInfo.getDstInspectionVmtTerminal(dstTerminal);
                    mtService = dstTerminal;
                } else {
                    inspectMtVmtService = dstTerminal;
                    mtService = srcTerminal;
                }

                if (mtService.existInspectFail() || inspectMtVmtService.existInspectFail()) {
                    cancelInspection = true;
                }
            } else {
                System.out.println("processInspectionOkStatus, not discussion .....");
                if (dstTerminal.existInspectFail()) {
                    System.out.println("processInspectionOkStatus, not discussion, exist inspect failed");
                    cancelInspection = true;
                }
            }

            System.out.println("processInspectionOkStatus, all mode, cancelInspection:"+cancelInspection);
            if (cancelInspection){
                mcuRestClientService.cancelInspection(groupConfInfo.getConfId(), inspectionOkMode.getName(), dstTerminal.getMtId());
                return;
            }

            if (inspectionOkMode == InspectionModeEnum.VIDEO && InspectionStatusEnum.OK.getCode() == dstTerminal.getInspectAudioStatus()
                    || inspectionOkMode == InspectionModeEnum.AUDIO && InspectionStatusEnum.OK.getCode() == dstTerminal.getInspectVideoStatus()) {
                System.out.println("processInspectionOkStatus, video and audio inspection Ok! terminal:"+dstTerminal.getE164());
                dstTerminal.setInspectionStatus(InspectionStatusEnum.OK);
            }
        } else {
            System.out.println(" processInspectionOkStatus : invalid inpsection mode!!");
        }

        if (dstTerminal.isInspection()){
            System.out.println("processInspectionOkStatus, set src been inspected!!! srcE164:"+srcTerminal.getE164());
            srcTerminal.setInspectedStatus(InspectionStatusEnum.OK);
            if (null == srcTerminal.getInspectedParam(dstTerminal.getE164())) {
                InspectedParam inspectedParam = new InspectedParam();
                inspectedParam.setVmt(dstTerminal.isVmt());
                inspectedParam.setStatus(InspectionStatusEnum.OK);
                srcTerminal.addInspentedTerminal(dstTerminal.getE164(), inspectedParam);
            } else {
                srcTerminal.setInspectedStatus(dstTerminal.getE164(), InspectionStatusEnum.OK);
            }

            terminalMediaSourceService.addGroupInspectionParam(dstTerminal.getE164(), dstTerminal.getInspectionParam());
        }
    }

    private void processInspectionOK(SubscribeEvent subscribeEvent, GroupConfInfo groupConfInfo, boolean bDiscussion, BaseRequestMsg<? extends BaseResponseMsg> requestMsg) {
        //选看成功, 通道中的mt_id为选看目的设备ID
        String[] parseResult = subscribeEvent.getChannel().split("/");
        int mode = Integer.valueOf(parseResult[5]);
        String dstMtId = parseResult[4];
        System.out.println("processInspectionOK, channel:"+subscribeEvent.getChannel()+", mtId:"+dstMtId+", mode:"+mode);
        TerminalService dstTerminal = groupConfInfo.getDstInspectionTerminal(dstMtId);
        TerminalService srcTerminal = groupConfInfo.getSrcInspectionTerminal(dstTerminal);
        String confId = groupConfInfo.getConfId();

        if (null == srcTerminal){
            System.out.println("processInspectionOK, null == srcTerminal!");
        } else {
            System.out.println("processInspectionOK, terminal[" + dstTerminal.getE164() + "] inspect terminal[" + srcTerminal.getE164() + "] ok, mode:" + mode);
        }

        if (mode == InspectionModeEnum.VIDEO.getCode()) {
            processInspectionOkStatus(InspectionModeEnum.VIDEO, groupConfInfo, bDiscussion, srcTerminal, dstTerminal);
        } else if (mode == InspectionModeEnum.AUDIO.getCode()) {
            processInspectionOkStatus(InspectionModeEnum.AUDIO, groupConfInfo, bDiscussion, srcTerminal, dstTerminal);
        } else {
            return;
        }

        if (null == requestMsg) {
            System.out.println("processInspectionOK, null == requestMsg");
            return;
        }

        if (requestMsg instanceof InspectionRequest) {
            System.out.println("processInspectionOK, start deal inspectionRequest, dstTerminal["+dstTerminal.getE164()+"] isInspection : " + dstTerminal.isInspection());
            InspectionRequest inspectionRequest = (InspectionRequest) requestMsg;
            InspectionParam inspectionParam = inspectionRequest.getInspectionParam();
            if (dstTerminal.isInspection()) {
                if (dstTerminal.isVmt()) {
                    System.out.println("processInspectionOK, dst is vmt, set reverse channel!");
                    List<DetailMediaResouce> reverseDetailResource = dstTerminal.getReverseChannel();
                    inspectionRequest.makeSuccessResponseMsg(TerminalMediaResource.convertToMediaResource(reverseDetailResource, inspectionParam.getMode()));
                } else if (srcTerminal.isVmt()){
                    System.out.println("processInspectionOK, src is vmt, set forward channel!");
                    List<DetailMediaResouce> forwardDetailResource = srcTerminal.getForwardChannel();
                    inspectionRequest.makeSuccessResponseMsg(TerminalMediaResource.convertToMediaResource(forwardDetailResource, inspectionParam.getMode()));
                } else {
                    //会议终端选看会议终端
                    inspectionRequest.makeSuccessResponseMsg();
                }
            }
            return;
        }

        //如果全部成功，在此回应成功
        TerminalService inspectMtVmtService;
        TerminalService mtService;
        if (srcTerminal.isVmt()){
            //如果源是虚拟终端，则dstTermial为会议终端，获取选看该会议终端的虚拟终端
            inspectMtVmtService = groupConfInfo.getDstInspectionVmtTerminal(dstTerminal);
            mtService = dstTerminal;
        } else {
            inspectMtVmtService = dstTerminal;
            mtService = srcTerminal;
        }

        InspectionStatusEnum mtInspectedStatus = mtService.getInspectedParam(inspectMtVmtService.getE164()).getStatus();
        System.out.println("processInspectionOK, discussionGroupRequest, mtTerminal[e164:"+mtService.getE164()+",inspection:"+mtService.isInspection()+",inspected:"+mtInspectedStatus.getName()+"]");
        JoinDiscussionGroupRequest joinDiscussionGroupRequest = (JoinDiscussionGroupRequest) requestMsg;
        if (mtService.isInspection() && mtInspectedStatus == InspectionStatusEnum.OK) {
            processDiscussionOk(joinDiscussionGroupRequest, groupConfInfo, inspectMtVmtService, mtService);
            return;
        }

        System.out.println("processInspectionOK, discussionGroupRequest, mtInspectStatus[v:"+mtService.getInspectVideoStatus()+", a:"+mtService.getInspectAudioStatus()+"]");
        System.out.println("dstVmtInspectStatus[v:"+inspectMtVmtService.getInspectVideoStatus()+", a:"+inspectMtVmtService.getInspectAudioStatus()+"]");
        //如果其他三个方向都已经收到更新消息，且存在失败，则在此回应消息
        int unknownStatus = InspectionStatusEnum.UNKNOWN.getCode();
        if ((mtService.existInspectFail() || inspectMtVmtService.existInspectFail())
                && unknownStatus != mtService.getInspectAudioStatus()
                && unknownStatus != mtService.getInspectVideoStatus()
                && unknownStatus != inspectMtVmtService.getInspectAudioStatus()
                && unknownStatus != inspectMtVmtService.getInspectVideoStatus()) {

            processDiscussionFail(joinDiscussionGroupRequest);
        }
    }

    private void processJoinDiscussionRequest(SubscribeEvent subscribeEvent, GroupConfInfo groupConfInfo, JoinDiscussionGroupRequest joinDiscussionGroupRequest) {
        //选看通道为 /confs/{conf_id}/inspections/{mt_id}/{mode}
        if (subscribeEvent.getMethod().equals(SubscribeMethodEnum.DELETE.getName())) {
            //在加入讨论组的处理过程中，不处理取消选看
            return;
        }

        System.out.println("processJoinDiscussionRequest, channel:"+subscribeEvent.getChannel()+", method:"+subscribeEvent.getMethod());
        joinDiscussionGroupRequest.removeMsg(subscribeEvent.getChannel());
        groupConfInfo.delWaitDealTask(subscribeEvent.getChannel());

        if (subscribeEvent.getChannel().contains("mts")) {
            //说明是终端列表通道
            System.out.println("processJoinDiscussionRequest, terminal status channel!!!");
            if (subscribeEvent.getMethod().equals(SubscribeMethodEnum.NOTIFY.getName())) {
                //设备加入会议失败 /confs/{conf_id}/mts
                System.out.println("processJoinDiscussionRequest, add mt into conf failed, errcode : " + subscribeEvent.getErrorCode());
                if (joinDiscussionGroupRequest.getWaitMsg().isEmpty()) {
                    McuStatus mcuStatus = McuStatus.resolve(subscribeEvent.getErrorCode());
                    joinDiscussionGroupRequest.makeErrorResponseMsg(ConfInterfaceResult.ADD_TERMINAL_INTO_CONFERENCE.getCode(), HttpStatus.OK, mcuStatus.getDescription());
                }
                return;
            }

            //成功，则开始进行相互选看 /confs/{conf_id}/cascades/{cascade_id}/mts/{mt_id}
            boolean bMutualInspection = true;
            String[] parseResult = subscribeEvent.getChannel().split("/");
            String vmtMtId = parseResult[6];
            String vmtE164 = groupConfInfo.getE164(vmtMtId);
            TerminalService vmtService = groupConfInfo.getAndUseVmt(vmtE164);
            String mtE164 = null;
            InspectionSrcParam inspectionSrcParam = vmtService.getInspectionParam();
            if (null != inspectionSrcParam){
                mtE164 = inspectionSrcParam.getMtE164();
            }else {
                bMutualInspection = false;
            }

            ConcurrentHashMap<String, InspectedParam> inspectedTerminals = vmtService.getInspentedTerminals();
            //虚拟终端只会被一个会议终端选看，且不会被虚拟终端选看，因此inspectedTerminals里面应该有且仅有一个结点信息
            for (Map.Entry<String, InspectedParam> inspectedParamEntry : inspectedTerminals.entrySet()){
                if (null == mtE164){
                    mtE164  = inspectedParamEntry.getKey();
                } else if (!inspectedParamEntry.getKey().equals(mtE164)) {
                    //如果选看虚拟终端的会议终端与虚拟终端选看的会议终端不一致，则说明该虚拟终端加入讨论组
                    //仅仅是作为会议终端的选看源加入，此时只需要完成会议终端选看虚拟终端即可
                    mtE164  = inspectedParamEntry.getKey();
                    bMutualInspection = false;
                }

                break;
            }

            TerminalService mtService = groupConfInfo.getMtMember(mtE164);
            System.out.println("processJoinDiscussionRequest, start discussion, mt:"+mtService.getE164()+", vmt:"+vmtE164);
            McuStatus mcuStatus = confInterfaceService.startInspectionForDiscusion(groupConfInfo, mtService, vmtService, bMutualInspection, joinDiscussionGroupRequest);
            if (mcuStatus.getValue() > 0 && joinDiscussionGroupRequest.getWaitMsg().isEmpty()){
                joinDiscussionGroupRequest.makeErrorResponseMsg(ConfInterfaceResult.INSPECTION.getCode(), HttpStatus.OK, mcuStatus.getDescription());
            }
            return;
        }

        if (subscribeEvent.getMethod().equals(SubscribeMethodEnum.NOTIFY.getName())) {
            processInspectionFail(subscribeEvent, groupConfInfo, joinDiscussionGroupRequest);
            return;
        }

        if (subscribeEvent.getMethod().equals(SubscribeMethodEnum.UPDATE.getName())) {
            processInspectionOK(subscribeEvent, groupConfInfo, true, joinDiscussionGroupRequest);
        }
    }

    private boolean processCancelInspection(SubscribeEvent subscribeEvent, GroupConfInfo groupConfInfo) {
        String[] parseResult = subscribeEvent.getChannel().split("/");
        int mode = Integer.valueOf(parseResult[5]);
        String dstMtId = parseResult[4];
        TerminalService dstTerminal = groupConfInfo.getDstInspectionTerminal(dstMtId);
        TerminalService srcTerminal = groupConfInfo.getSrcInspectionTerminal(dstTerminal);
        InspectionSrcParam inspectionSrcParam = dstTerminal.getInspectionParam();
        if (null == inspectionSrcParam){
            return true;
        }

        String inspectMode = inspectionSrcParam.getMode();
        if (null != srcTerminal){
            System.out.println("processCancelInspection, dstTerminal(E164:"+dstTerminal.getE164()+", MtId:"+dstMtId+"), srcTerminal(E164:"+srcTerminal.getE164()+", MtId:"+srcTerminal.getMtId()+"), mode:"+mode+", inspectMode:"+inspectMode);
        } else {
            System.out.println("processCancelInspection, dstTerminal(E164:" + dstTerminal.getE164() + ", MtId:" + dstMtId + "), mode:" + mode + ", inspectMode:" + inspectMode);
        }

        boolean bStopInspection = false;
        boolean bUpdateInspectionParam = false;
        if (mode == InspectionModeEnum.VIDEO.getCode()){
            dstTerminal.setInspectVideoStatus(InspectionStatusEnum.CANCELOK.getCode());
            if (inspectMode.equals(InspectionModeEnum.VIDEO.getName())){
                //如果选看模式也是视频，则只要收到该消息，则认为处理完毕
                bStopInspection = true;
            } else if (inspectMode.equals(InspectionModeEnum.ALL.getName())){
                //如果选看模式为音视频，则需要判断音频选看状态
                if (dstTerminal.getInspectAudioStatus() == InspectionStatusEnum.FAIL.getCode()
                        ||  dstTerminal.getInspectAudioStatus() == InspectionStatusEnum.CANCELOK.getCode()){
                    bStopInspection = true;
                }
            } else {
                bUpdateInspectionParam = true;
            }
        } else if (mode == InspectionModeEnum.AUDIO.getCode()){
            dstTerminal.setInspectAudioStatus(InspectionStatusEnum.CANCELOK.getCode());
            if (inspectMode.equals(InspectionModeEnum.AUDIO.getName())){
                //如果选看模式为音频，则收到该消息，则认为处理完毕
                bStopInspection = true;
            } else if (inspectMode.equals(InspectionModeEnum.ALL.getName())){
                //如果选看模式为音视频，则需要判断视频选看状态
                if (dstTerminal.getInspectVideoStatus() == InspectionStatusEnum.FAIL.getCode()
                        ||  dstTerminal.getInspectVideoStatus() == InspectionStatusEnum.CANCELOK.getCode()){
                    bStopInspection = true;
                }
            } else {
                bUpdateInspectionParam = true;
            }
        }

        if (bStopInspection){
            System.out.println("processCancelInspection, bStopInspection, terminal("+dstTerminal.getE164()+") remove inspection param! srcTerminal:"+srcTerminal.getE164());
            terminalMediaSourceService.delGroupInspectionParam(dstTerminal.getE164());
            dstTerminal.setInspectionStatus(InspectionStatusEnum.FAIL);
            dstTerminal.setInspectionParam(null);

            if (dstTerminal.isVmt()
                    && (dstTerminal.getInspentedTerminals() == null || dstTerminal.getInspentedTerminals().isEmpty())){
                //如果虚拟终端没有选看终端，且没有被选看，则放回空闲队列
                System.out.println("processCancelInspection, bStopInspection, dst terminal("+dstTerminal.getE164()+") is vmt and need free");

                groupConfInfo.freeVmt(dstTerminal.getE164());
            }

            if(null != srcTerminal) {
                srcTerminal.delInspentedTerminal(dstTerminal.getE164());
                if (srcTerminal.isVmt()
                        && srcTerminal.getInspentedTerminals().isEmpty()
                        && srcTerminal.getInspectionParam() == null){
                    //如果选看源是虚拟终端且没有选看和被选看，则放回空闲队列
                    System.out.println("processCancelInspection, bStopInspection, src terminal("+srcTerminal.getE164()+") is vmt and need free");
                    groupConfInfo.freeVmt(srcTerminal.getE164());
                }
            }
            return true;
        }

        if (bUpdateInspectionParam){
            terminalMediaSourceService.addGroupInspectionParam(dstTerminal.getE164(), dstTerminal.getInspectionParam());
            return true;
        }

        return false;
    }

    private void processLeftDiscussionRequest(SubscribeEvent subscribeEvent, GroupConfInfo groupConfInfo, LeftDiscussionGroupRequest leftDiscussionGroupRequest) {
        leftDiscussionGroupRequest.removeMsg(subscribeEvent.getChannel());
        groupConfInfo.delWaitDealTask(subscribeEvent.getChannel());

        //退出讨论组时，会停止选看，因此只应该处理删除消息
        if (subscribeEvent.getMethod().equals(SubscribeMethodEnum.DELETE.getName())) {
            processCancelInspection(subscribeEvent, groupConfInfo);
        }
    }

    private void processInspectionRequest(SubscribeEvent subscribeEvent, GroupConfInfo groupConfInfo, InspectionRequest inspectionRequest) {
        //如果是终端列表通道信息，则在收到该消息后，开始选看
        System.out.println("processInspectionRequest, channel:"+subscribeEvent.getChannel()+", method:"+subscribeEvent.getMethod());
        inspectionRequest.removeMsg(subscribeEvent.getChannel());
        groupConfInfo.delWaitDealTask(subscribeEvent.getChannel());

        if (subscribeEvent.getChannel().contains("mts")) {
            //说明是终端列表通道
            System.out.println("processInspectionRequest, terminal status channel!!!");
            if (subscribeEvent.getMethod().equals(SubscribeMethodEnum.NOTIFY.getName())) {
                //设备加入会议失败 /confs/{conf_id}/mts
                System.out.println("processInspectionRequest, add mt into conf failed, errcode : " + subscribeEvent.getErrorCode());
                inspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.ADD_TERMINAL_INTO_CONFERENCE.getCode(), HttpStatus.OK, ConfInterfaceResult.ADD_TERMINAL_INTO_CONFERENCE.getMessage());
                return;
            }

            //成功，则开始进行选看 /confs/{conf_id}/cascades/{cascade_id}/mts/{mt_id}
            String[] parseResult = subscribeEvent.getChannel().split("/");
            InspectionParam inspectionParam = inspectionRequest.getInspectionParam();
            String srcE164 = inspectionParam.getSrcMtE164();
            String dstE164 = inspectionParam.getDstMtE164();

            if (dstE164.isEmpty()) {
                //说明是监控前端选看会议终端,因此会选择一个虚拟终端入会并选看会议终端,因此此次上线的是目的虚拟终端
                TerminalService srcTerminal = groupConfInfo.getMtMember(srcE164);
                if (!srcTerminal.isOnline()) {
                    inspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.OFFLINE.getCode(), HttpStatus.OK, ConfInterfaceResult.OFFLINE.getMessage());
                    return;
                }

                System.out.println("processInspectionRequest, dst terminal(" + parseResult[6]+") is online, start inspection!");
                confInterfaceService.inspectionMt(groupConfInfo, inspectionParam.getMode(), srcTerminal.getMtId(), parseResult[6], inspectionRequest);
            } else if (srcE164.isEmpty()){
                //说明是会议终端浏览监控前端,需要选择一个虚拟终端,然后让会议终端选看该虚拟终端,因此此处上线的是源虚拟终端
                TerminalService dstTerminal = groupConfInfo.getMtMember(dstE164);
                if (!dstTerminal.isOnline()){
                    inspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.OFFLINE.getCode(), HttpStatus.OK, ConfInterfaceResult.OFFLINE.getMessage());
                    return;
                }
                System.out.println("processInspectionRequest, src terminal(" + parseResult[6]+") is online, start inspection!");
                confInterfaceService.inspectionMt(groupConfInfo, inspectionParam.getMode(), parseResult[6], dstTerminal.getMtId(), inspectionRequest);
            }
            return;
        }

        //选看通道 /confs/{conf_id}/inspections/{mt_id}/{mode}
        if (subscribeEvent.getMethod().equals(SubscribeMethodEnum.NOTIFY.getName())) {
            processInspectionFail(subscribeEvent, groupConfInfo, inspectionRequest);
            return;
        }

        if (subscribeEvent.getMethod().equals(SubscribeMethodEnum.UPDATE.getName())) {
            processInspectionOK(subscribeEvent, groupConfInfo, false, inspectionRequest);
        }
    }

    private void processCancelInspectionRequest(SubscribeEvent subscribeEvent, GroupConfInfo groupConfInfo, CancelInspectionRequest cancelInspectionRequest){
        //选看通道 /confs/{conf_id}/inspections/{mt_id}/{mode}
        cancelInspectionRequest.removeMsg(subscribeEvent.getChannel());
        groupConfInfo.delWaitDealTask(subscribeEvent.getChannel());

        if (subscribeEvent.getMethod().equals(SubscribeMethodEnum.DELETE.getName())){
            //取消选看时，只需要处理delete方法，收到该方法，则认为取消成功
            boolean bCancelInspection = processCancelInspection(subscribeEvent, groupConfInfo);

            if (bCancelInspection)
                cancelInspectionRequest.makeSuccessResponseMsg();
        }
    }

    private void processStartDualStreamRequest(SubscribeEvent subscribeEvent, GroupConfInfo groupConfInfo, StartDualStreamRequest startDualStreamRequest){
        //双流源通道 /confs/{conf_id}/dualstream
        startDualStreamRequest.removeMsg(subscribeEvent.getChannel());
        groupConfInfo.delWaitDealTask(subscribeEvent.getChannel());

        if (subscribeEvent.getMethod().equals(SubscribeMethodEnum.UPDATE.getName())){
            //获取双流源
            String mtId = mcuRestClientService.getDualStream(groupConfInfo.getConfId());
            String mtE164 = startDualStreamRequest.getDualStreamParam().getMtE164();
            TerminalService terminalService = groupConfInfo.getBroadcastVmtService();
            List<DetailMediaResouce> resources;
            String dualMtId;

            if (mtE164.isEmpty()){
                //获取广播虚拟终端
                dualMtId = terminalService.getMtId();
                resources = terminalService.getForwardChannel();
            } else {
                dualMtId = groupConfInfo.getMtMember(mtE164).getMtId();
                resources = terminalService.getReverseChannel();
            }

            if (!mtId.equals(dualMtId)){
                //如果双流源与请求的设备不一致，忽略
                return;
            }

            int tryTimes = 10;
            do {
                for (DetailMediaResouce detailMediaResouce : resources) {
                    if (detailMediaResouce.getDual() == 0) {
                        continue;
                    }

                    MediaResource mediaResource = new MediaResource();
                    mediaResource.setId(detailMediaResouce.getId());
                    mediaResource.setType(detailMediaResouce.getType());
                    mediaResource.setDual(true);

                    startDualStreamRequest.addResource(mediaResource);
                    startDualStreamRequest.makeSuccessResponseMsg();
                    return;
                }

                try {
                    TimeUnit.MICROSECONDS.sleep(300);
                } catch (Exception e){
                }

            }while (--tryTimes > 0);

            startDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.CTRL_DUALSTREAM.getCode(), HttpStatus.OK, ConfInterfaceResult.CTRL_DUALSTREAM.getMessage());
        }
    }

    private void processCancelDualStreamRequest(SubscribeEvent subscribeEvent, GroupConfInfo groupConfInfo, CancelDualStreamRequest cancelDualStreamRequest){
        //双流源通道 /confs/{conf_id}/dualstream
        cancelDualStreamRequest.removeMsg(subscribeEvent.getChannel());
        groupConfInfo.delWaitDealTask(subscribeEvent.getChannel());

        if (subscribeEvent.getMethod().equals(SubscribeMethodEnum.DELETE.getName())){
            cancelDualStreamRequest.makeSuccessResponseMsg();
        }
    }

    @Autowired
    private ConfInterfaceService confInterfaceService;

    @Autowired
    private McuRestClientService mcuRestClientService;

    @Autowired
    private TerminalMediaSourceService terminalMediaSourceService;

    @Autowired
    private ConfInterfacePublishService confInterfacePublishService;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
}
