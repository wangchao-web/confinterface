package com.kedacom.confinterface.h323;


import com.kedacom.confadapter.IConferenceEventHandler;
import com.kedacom.confadapter.common.ConferenceInfo;
import com.kedacom.confadapter.common.ConferencePresentParticipant;
import com.kedacom.confadapter.media.MediaDescription;
import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dao.InspectionSrcParam;
import com.kedacom.confinterface.dto.*;
import com.kedacom.confinterface.inner.*;
import com.kedacom.confinterface.restclient.mcu.InspectionStatusEnum;
import com.kedacom.confinterface.service.*;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class RegisterGkHandler implements Runnable {

    public void setH323TerminalService(H323TerminalService h323TerminalService) {
        this.h323TerminalService = h323TerminalService;
    }

    public static List<Future<Boolean>> getFutureList() {
        return futureList;
    }

    private H323TerminalService h323TerminalService;

    private static List<Future<Boolean>> futureList = Collections.synchronizedList(new ArrayList<>());

    @Override
    public synchronized void run() {
        Future<Boolean> regFuture = h323TerminalService.startRegGK();
        try {
            if (null != regFuture && regFuture.get()) {
                futureList.add(regFuture);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class H323TerminalManageService extends TerminalManageService implements IConferenceEventHandler {

    public H323TerminalManageService(H323ProtocalConfig h323ProtocalConfig) {
        super();
        this.protocalConfig = h323ProtocalConfig;
    }

    public H323ProtocalConfig getProtocalConfig() {
        return protocalConfig;
    }

    @Override
    public TerminalService createTerminal(String e164, boolean bVmt) {

        StringBuffer name = new StringBuffer();

        if (bVmt) {
            name.append(protocalConfig.getBaseSysConfig().getVmtNamePrefix());
            name.append(e164);
        } else {
            name.append("h323mt_");
            name.append(e164);
        }

        H323TerminalService h323TerminalService = new H323TerminalService(e164, name.toString(), bVmt, protocalConfig);
        createConfParticipant(h323TerminalService);
        if (bVmt)
            freeVmtServiceMap.put(e164, h323TerminalService);

        return h323TerminalService;
    }

    @Override
    public void StartUp() {
        //如果需要注册GK，则在此处完成所有虚拟终端的注册
        if (!protocalConfig.isUseGK()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"[H323VmtManageService] no need register gk");
            System.out.println("[H323VmtManageService] no need register gk");
            return;
        }

        int registerGkTerminalNum = freeVmtServiceMap.size() + usedVmtServiceMap.size();
        ExecutorService queueThreadPool = Executors.newFixedThreadPool(protocalConfig.getRegisterGkThreadNum());
        for (TerminalService h323VmtService : freeVmtServiceMap.values()) {
            RegisterGkHandler registerGkHandler = new RegisterGkHandler();
            registerGkHandler.setH323TerminalService((H323TerminalService) h323VmtService);
            queueThreadPool.execute(registerGkHandler);
        }

        //会议接入微服务异常重启后的Gk注册会涉及到usedVmtServiceMap
        for (TerminalService h323UsedVmtService : usedVmtServiceMap.values()) {
            RegisterGkHandler registerGkHandler = new RegisterGkHandler();
            registerGkHandler.setH323TerminalService((H323TerminalService) h323UsedVmtService);
            queueThreadPool.execute(registerGkHandler);
        }

        queueThreadPool.shutdown();

        List<Future<Boolean>> futureList = RegisterGkHandler.getFutureList();
        while (true) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"futureList size : " + futureList.size() + ", freeVmt : " + registerGkTerminalNum);
            System.out.println("futureList size : " + futureList.size() + ", freeVmt : " + registerGkTerminalNum);
            if (futureList.size() == registerGkTerminalNum)
                break;

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {

            }
        }
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"[H323VmtManageService] finished start up, time : " + System.currentTimeMillis());
        System.out.println("[H323VmtManageService] finished start up, time : " + System.currentTimeMillis());
    }

    @Override
    @Async("confTaskExecutor")
    public void OnInvited(String participantid, ConferenceInfo conferenceInfo) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnInvited, participantid : " + participantid + ", confId : " + conferenceInfo.getId() + ", threadName:" + Thread.currentThread().getName());
        System.out.println("OnInvited, participantid : " + participantid + ", confId : " + conferenceInfo.getId() + ", threadName:" + Thread.currentThread().getName());
        TerminalService terminalService = usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnInvited, not found participant!!");
            System.out.println("OnInvited, not found participant!!");
            return;
        }

        terminalService.setConfId(conferenceInfo.getId());
        terminalService.allowExtensiveStream();

        return;
    }

    @Override
    @Async("confTaskExecutor")
    public void OnParticipantJoined(String participantid, ConferencePresentParticipant conferencePresentParticipant) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnParticipantJoined, terminal: " + conferencePresentParticipant.getId() + "join conference! confId : "
                + conferencePresentParticipant.getConf().getId() + ", threadName:" + Thread.currentThread().getName());
        System.out.println("OnParticipantJoined, terminal: " + conferencePresentParticipant.getId() + "join conference! confId : "
                + conferencePresentParticipant.getConf().getId() + ", threadName:" + Thread.currentThread().getName());
    }

    @Override
    @Async("confTaskExecutor")
    public void OnParticipantLeft(String participantid, ConferencePresentParticipant conferencePresentParticipant) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnParticipantLeft, terminal: " + conferencePresentParticipant.getId() + "left conference! confId: "
                + conferencePresentParticipant.getConf().getId() + ", threadName:" + Thread.currentThread().getName());
        System.out.println("OnParticipantLeft, terminal: " + conferencePresentParticipant.getId() + "left conference! confId: "
                + conferencePresentParticipant.getConf().getId() + ", threadName:" + Thread.currentThread().getName());
        /*如果下线的是虚拟终端,则不需要额外的处理,因为虚拟终端不会选看或者被其他虚拟终端选看*/
        String offlineMtE164 = conferencePresentParticipant.getId();
        TerminalService offlineTerminal = findVmt(offlineMtE164);
        if (null != offlineTerminal) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnParticipantLeft, offline terminal is vmt!!");
            System.out.println("OnParticipantLeft, offline terminal is vmt!!");
            return;
        }

        TerminalService terminalService = usedVmtServiceMap.get(participantid);
        if (terminalService.isInspection()) {
            //判断选看的是否是下线的终端，如果是，则需要将选看状态清除
            InspectionSrcParam inspectionParam = terminalService.getInspectionParam();
            if (inspectionParam.getMtE164().equals(offlineMtE164)) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnParticipantLeft, inspection left terminal! dstMt:" + participantid);
                System.out.println("OnParticipantLeft, inspection left terminal! dstMt:" + participantid);
                terminalService.setInspectionStatus(InspectionStatusEnum.UNKNOWN);
                terminalService.setInspectAudioStatus(InspectionStatusEnum.UNKNOWN.getCode());
                terminalService.setInspectVideoStatus(InspectionStatusEnum.UNKNOWN.getCode());

                //清除数据库中的选看资源
                terminalMediaSourceService.delGroupInspectionParam(participantid);
            }
        }

        if (terminalService.isInspected()) {
            //是否被下线的终端选看
            InspectedParam inspectedParam = terminalService.getInspectedParam(offlineMtE164);
            if (null == inspectedParam)
                return;

            terminalService.delInspentedTerminal(offlineMtE164);
        }
    }

    @Override
    @Async("confTaskExecutor")
    public void OnKickedOff(String participantid) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnKickedOff, terminal: " + participantid + " is kicked off conference, threadName: " + Thread.currentThread().getName());
        System.out.println("OnKickedOff, terminal: " + participantid + " is kicked off conference, threadName: " + Thread.currentThread().getName());
        H323TerminalService terminalService = (H323TerminalService) usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnKickedOff, not found terminal!");
            System.out.println("OnKickedOff, not found terminal!");
            return;
        }

        usedVmtServiceMap.remove(participantid);
        freeVmtServiceMap.put(participantid, terminalService);
        Map<String, P2PCallGroup> p2pCallGroupMap = ConfInterfaceService.p2pCallGroupMap;
        if(null != p2pCallGroupMap){
             for (Map.Entry<String, P2PCallGroup> entry : p2pCallGroupMap.entrySet()) {
                 for(Map.Entry<String, TerminalService> entrys : entry.getValue().getCallMap().entrySet()){
                     if(entrys.getValue() == terminalService){
                         LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"entrys.getKey() "+entrys.getKey());
                         System.out.println("entrys.getKey() "+entrys.getKey());
                         entry.getValue().removeCallMember(entrys.getKey());
                         if ("mediaSchedule".equals(protocalConfig.getBaseSysConfig().getPushServiceType())) {
                             TerminalStatusNotify terminalStatusNotify = new TerminalStatusNotify();
                             TerminalStatus terminalStatus = new TerminalStatus(entrys.getKey(), "MT", 0);
                             terminalStatusNotify.addMtStatus(terminalStatus);
                             LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"terminalService.getE164() "+terminalService.getE164()+ ",terminalService.getGroupId() : "+terminalService.getGroupId());
                             System.out.println("terminalService.getE164() "+terminalService.getE164()+ ",terminalService.getGroupId() : "+terminalService.getGroupId());
                             confInterfacePublishService.publishMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS, terminalService.getGroupId(), terminalStatusNotify);
                         } else {
                             UnifiedDevicePushTerminalStatus unifiedDevicePushTerminalStatus = new UnifiedDevicePushTerminalStatus(entrys.getKey(), terminalService.getGroupId(), TerminalOnlineStatusEnum.OFFLINE.getCode());
                             unifiedDevicePushService.publishMtStatus(unifiedDevicePushTerminalStatus);
                         }
                     }
                 }
            }
        }

        //释放该虚拟终端的所有交换资源
        synchronized (terminalService) {
            terminalService.clearExchange();

            //移除数据库中的资源
            terminalMediaSourceService.delTerminalMediaResource(participantid);
            terminalMediaSourceService.delGroupInspectionParam(participantid);
            terminalMediaSourceService.delGroupVmtMember(terminalService.getGroupId(), participantid);

            terminalService.leftConference();
        }
    }

    @Override
    @Async("confTaskExecutor")
    public void OnLocalMediaRequested(String participantid, Vector<MediaDescription> mediaDescriptions) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnLocalMediaRequested, request terminal: " + participantid + " local media! threadName:" + Thread.currentThread().getName());
        System.out.println("OnLocalMediaRequested, request terminal: " + participantid + " local media! threadName:" + Thread.currentThread().getName());
        H323TerminalService terminalService = (H323TerminalService) usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnLocalMediaRequested, not found terminal : " + participantid);
            System.out.println("OnLocalMediaRequested, not found terminal : " + participantid);
            return;
        }

        boolean bOK = terminalService.onOpenLogicalChannel(mediaDescriptions);
        if (!bOK) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnLocalMediaRequested, onOpenLogicalChannel failed! participantid :" + participantid);
            System.out.println("OnLocalMediaRequested, onOpenLogicalChannel failed! participantid :" + participantid);
        }

        //将虚拟终端的资源更新到数据库中
        synchronized (terminalService) {
            TerminalMediaResource oldTerminalMediaResource = terminalMediaSourceService.getTerminalMediaResource(participantid);
            List<MediaResource> forwardResources = TerminalMediaResource.convertToMediaResource(terminalService.getForwardChannel(), "all");
            List<MediaResource> reverseResources = TerminalMediaResource.convertToMediaResource(terminalService.getReverseChannel(), "all");
            boolean bNeedUpdate = false;
            if (null == oldTerminalMediaResource) {
                bNeedUpdate = true;
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnLocalMediaRequested, null == oldTerminalMediaResource, need update! E164:" + participantid + ", forwardResources:" + forwardResources + ", reverseResources:" + reverseResources);
                System.out.println("OnLocalMediaRequested, null == oldTerminalMediaResource, need update! E164:" + participantid + ", forwardResources:" + forwardResources + ", reverseResources:" + reverseResources);
            } else {
                List<MediaResource> oldForwardResources = oldTerminalMediaResource.getForwardResources();
                if (null == oldForwardResources || oldForwardResources.size() != forwardResources.size()) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnLocalMediaRequested, 1. forward resource need update! " + "E164:" + participantid + ", oldResources:" + oldForwardResources + ", newResource:" + forwardResources);
                    System.out.println("OnLocalMediaRequested, 1. forward resource need update! " + "E164:" + participantid + ", oldResources:" + oldForwardResources + ", newResource:" + forwardResources);
                    bNeedUpdate = true;
                } else {
                    oldForwardResources.removeAll(forwardResources);
                    if (!oldForwardResources.isEmpty()) {
                        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnLocalMediaRequested, 2. forward resource need update! " + "E164:" + participantid + ", oldResources:" + oldForwardResources + ", newResource:" + forwardResources);
                        System.out.println("OnLocalMediaRequested, 2. forward resource need update! " + "E164:" + participantid + ", oldResources:" + oldForwardResources + ", newResource:" + forwardResources);
                        bNeedUpdate = true;
                    } else {
                        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnLocalMediaRequested, forward resource no need update! " + "E164:" + participantid + ", Resources:" + forwardResources);
                        System.out.println("OnLocalMediaRequested, forward resource no need update! " + "E164:" + participantid + ", Resources:" + forwardResources);
                    }
                }

                List<MediaResource> oldReverseResources = oldTerminalMediaResource.getReverseResources();
                if (null == oldReverseResources || oldReverseResources.size() != reverseResources.size()) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnLocalMediaRequested, 1. reverse resource need update! " + "E164:" + participantid + ", oldResources:" + oldReverseResources + ", newResource:" + reverseResources);
                    System.out.println("OnLocalMediaRequested, 1. reverse resource need update! " + "E164:" + participantid + ", oldResources:" + oldReverseResources + ", newResource:" + reverseResources);
                    bNeedUpdate = true;
                } else {
                    oldReverseResources.removeAll(reverseResources);
                    if (!oldReverseResources.isEmpty()) {
                        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnLocalMediaRequested, 2. reverse resource need update! " + "E164:" + participantid + ", oldResources:" + oldReverseResources + ", newResource:" + reverseResources);
                        System.out.println("OnLocalMediaRequested, 2. reverse resource need update! " + "E164:" + participantid + ", oldResources:" + oldReverseResources + ", newResource:" + reverseResources);
                        bNeedUpdate = true;
                    } else {
                        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnLocalMediaRequested, reverse resource no need update! " + "E164:" + participantid + ", Resources:" + oldReverseResources);
                        System.out.println("OnLocalMediaRequested, reverse resource no need update! " + "E164:" + participantid + ", Resources:" + oldReverseResources);
                    }
                }
            }

            if (bNeedUpdate) {
                TerminalMediaResource terminalMediaResource = new TerminalMediaResource();
                terminalMediaResource.setMtE164(participantid);
                terminalMediaResource.setForwardResources(forwardResources);
                terminalMediaResource.setReverseResources(reverseResources);
                terminalMediaSourceService.setTerminalMediaResource(terminalMediaResource);
            }
        }
    }

    @Override
    @Async("confTaskExecutor")
    public void OnRemoteMediaReponsed(String participantid, Vector<MediaDescription> mediaDescriptions) {
        //该接口只有在使用H323协议时会用到
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnRemoteMediaReponsed, request terminal: " + participantid + " local media! threadName:" + Thread.currentThread().getName());
        System.out.println("OnRemoteMediaReponsed, request terminal: " + participantid + " local media! threadName:" + Thread.currentThread().getName());
        H323TerminalService terminalService = (H323TerminalService) usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnRemoteMediaReponsed， not found terminal : " + participantid);
            System.out.println("OnRemoteMediaReponsed， not found terminal : " + participantid);
            return;
        }

        boolean bOk = terminalService.updateExchange(mediaDescriptions);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"bOk : " + bOk);
        System.out.println("bOk : " + bOk);
        if (bOk) {
            if (!mediaDescriptions.get(0).getDual()) {
                if (null != terminalService.getRemoteMtAccount()) {
                    P2PCallRequestSuccess(terminalService, mediaDescriptions.get(0).getStreamIndex());
                }

                if (terminalService.getForwardGenericStreamNum().decrementAndGet() != 0)
                    return;

                ResumeDualStream(terminalService);
                return;
            }

            DualStreamRequestSuccess(terminalService, mediaDescriptions.get(0).getStreamIndex());
            return;
        }

        if (mediaDescriptions.get(0).getDual()) {
            DualStreamRequestFail(terminalService);
        } else if (null != terminalService.getRemoteMtAccount()) {
            P2PCallRequestFail(terminalService);
        }
    }

    @Override
    @Async("confTaskExecutor")
    public void OnMediaCleaned(String participantid, Vector<MediaDescription> mediaDescriptions) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnMediaCleaned, terminal: " + participantid + ", media cleaned, threadName: " + Thread.currentThread().getName());
        System.out.println("OnMediaCleaned, terminal: " + participantid + ", media cleaned, threadName: " + Thread.currentThread().getName());
        H323TerminalService terminalService = (H323TerminalService) usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnMediaCleaned, not found terminal!");
            System.out.println("OnMediaCleaned, not found terminal!");
            return;
        }

        List<String> resourceIds = new ArrayList<>();
        List<DetailMediaResouce> mediaResouces = terminalService.getReverseChannel();
        if (null == mediaResouces)
            return;

        for (MediaDescription mediaDescription : mediaDescriptions) {
            for (DetailMediaResouce mediaResouce : mediaResouces) {
                if (mediaResouce.getStreamIndex() != mediaDescription.getStreamIndex()) {
                    continue;
                }
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnMediaCleaned, type:" + mediaResouce.getType() + ", resourceId:" + mediaResouce.getId() + ", streamIndex:" + mediaDescription.getStreamIndex());
                System.out.println("OnMediaCleaned, type:" + mediaResouce.getType() + ", resourceId:" + mediaResouce.getId() + ", streamIndex:" + mediaDescription.getStreamIndex());
                resourceIds.add(mediaResouce.getId());
                break;
            }
        }

        if (resourceIds.isEmpty()) {
            return;
        }

        terminalService.removeExchange(resourceIds);

        synchronized (terminalService) {
            //删除对应的资源信息
            Iterator<String> resoureIdIterator = resourceIds.iterator();
            while (resoureIdIterator.hasNext()) {
                String resourceId = resoureIdIterator.next();
                mediaResouces = terminalService.getReverseChannel();
                if (null == mediaResouces)
                    break;

                for (DetailMediaResouce mediaResouce : mediaResouces) {
                    if (mediaResouce.getId().equals(resourceId)) {
                        mediaResouces.remove(mediaResouce);
                        resoureIdIterator.remove();
                        break;
                    }
                }
            }

            mediaResouces = terminalService.getReverseChannel();
            TerminalMediaResource oldTerminalMediaResource = terminalMediaSourceService.getTerminalMediaResource(participantid);
            if (null != oldTerminalMediaResource) {
                oldTerminalMediaResource.setReverseResources(TerminalMediaResource.convertToMediaResource(mediaResouces, "all"));
                terminalMediaSourceService.setTerminalMediaResource(oldTerminalMediaResource);
            }
        }

        resourceIds.clear();
    }

    private void ResumeDualStream(H323TerminalService terminalService) {
        //如果主流全部开启，判断是否需要恢复辅流
        if (terminalService.getResumeDualStream().compareAndSet(true, false)) {
            boolean bResumeOk = terminalService.resumeDualStream();
            if (bResumeOk)
                return;

            List<DetailMediaResouce> mediaResouces = terminalService.getForwardChannel();
            TerminalMediaResource oldTerminalMediaResource = terminalMediaSourceService.getTerminalMediaResource(terminalService.getE164());

            if (oldTerminalMediaResource.getForwardResources().size() > mediaResouces.size()) {
                oldTerminalMediaResource.setForwardResources(TerminalMediaResource.convertToMediaResource(mediaResouces, "all"));
                terminalMediaSourceService.setTerminalMediaResource(oldTerminalMediaResource);
            }
        }
    }

    private void DualStreamRequestSuccess(H323TerminalService terminalService, int dualStreamIndex) {
        //更新数据库中的正向双流交换资源信息
        List<DetailMediaResouce> mediaResouces = terminalService.getForwardChannel();
        TerminalMediaResource oldTerminalMediaResource = terminalMediaSourceService.getTerminalMediaResource(terminalService.getE164());
        oldTerminalMediaResource.setForwardResources(TerminalMediaResource.convertToMediaResource(mediaResouces, "all"));
        terminalMediaSourceService.setTerminalMediaResource(oldTerminalMediaResource);

        MediaResource mediaResource = new MediaResource();
        List<DetailMediaResouce> detailMediaResouces = terminalService.getForwardChannel();
        for (DetailMediaResouce detailMediaResouce : detailMediaResouces) {
            if (detailMediaResouce.getStreamIndex() != dualStreamIndex)
                continue;

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"OnRemoteMediaReponsed, dual streamIndex:" + dualStreamIndex);
            System.out.println("OnRemoteMediaReponsed, dual streamIndex:" + dualStreamIndex);
            mediaResource.setType(detailMediaResouce.getType());
            mediaResource.setDual(true);
            mediaResource.setId(detailMediaResouce.getId());

            break;
        }

        /*if (null == terminalService.getRemoteMtAccount()) {*/
            //非点对点呼叫
            StartDualStreamRequest startDualStreamRequest = (StartDualStreamRequest) terminalService.getWaitMsg(StartDualStreamRequest.class.getName());
            if (null == startDualStreamRequest)
                return;

            startDualStreamRequest.addResource(mediaResource);
            startDualStreamRequest.makeSuccessResponseMsg();
            terminalService.delWaitMsg(StartDualStreamRequest.class.getName());

            return;
        //}

       /* P2PCallRequest p2PCallRequest = (P2PCallRequest) terminalService.getWaitMsg(P2PCallRequest.class.getName());
        if (null == p2PCallRequest)
            return;

        p2PCallRequest.addForwardResource(mediaResource);
        p2PCallRequest.makeSuccessResponseMsg();
        terminalService.delWaitMsg(P2PCallRequest.class.getName());

        return;*/
    }

    private void DualStreamRequestFail(H323TerminalService terminalService) {
        //失败处理
        BaseRequestMsg dualStreamRequest;
        String waitMsgKey;
        if (null == terminalService.getRemoteMtAccount()) {
            waitMsgKey = StartDualStreamRequest.class.getName();
            dualStreamRequest = terminalService.getWaitMsg(waitMsgKey);
        } else {
            waitMsgKey = P2PCallRequest.class.getName();
            dualStreamRequest = terminalService.getWaitMsg(waitMsgKey);
        }

        if (null == dualStreamRequest)
            return;

        LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"50021 : update exchange node failed!");
        dualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.UPDATE_EXCHANGENODE_FAILED.getCode(), HttpStatus.OK, ConfInterfaceResult.UPDATE_EXCHANGENODE_FAILED.getMessage());
        terminalService.delWaitMsg(waitMsgKey);

        boolean bOk = terminalService.closeDualStreamChannel();
        if (bOk) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"DualStreamRequestFail, closeDualStreamChannel OK!");
            System.out.println("DualStreamRequestFail, closeDualStreamChannel OK!");
        } else {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"DualStreamRequestFail, closeDualStreamChannel failed!");
            System.out.println("DualStreamRequestFail, closeDualStreamChannel failed!");
        }
    }

    private void P2PCallRequestSuccess(H323TerminalService terminalService, int streamIndex) {
        P2PCallRequest p2PCallRequest = (P2PCallRequest) terminalService.getWaitMsg(P2PCallRequest.class.getName());
        if (null == p2PCallRequest)
            return;
        //System.out.println("p2PCallRequest : " +p2PCallRequest);
        List<DetailMediaResouce> mediaResouces = terminalService.getForwardChannel();
        for (DetailMediaResouce detailMediaResouce : mediaResouces) {
            //System.out.println("detailMediaResouce.getStreamIndex() != streamIndex : "+detailMediaResouce.getStreamIndex());
            //System.out.println("streamIndex : " +streamIndex);
            if (detailMediaResouce.getStreamIndex() != streamIndex)
                continue;

            MediaResource mediaResource = new MediaResource();
            mediaResource.setType(detailMediaResouce.getType());
            mediaResource.setDual(detailMediaResouce.getDual() == 1);
            mediaResource.setId(detailMediaResouce.getId());
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"mediaResource :" + mediaResource.getId());
            System.out.println("mediaResource :" + mediaResource.getId());
            p2PCallRequest.addForwardResource(mediaResource);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"添加正向资源");
            System.out.println("添加正向资源");
            p2PCallRequest.removeMsg(P2PCallRequest.class.getName());
            break;
        }

        if (p2PCallRequest.getWaitMsg().isEmpty())
            terminalService.delWaitMsg(P2PCallRequest.class.getName());
    }

    private void P2PCallRequestFail(H323TerminalService terminalService) {
        P2PCallRequest p2PCallRequest = (P2PCallRequest) terminalService.getWaitMsg(P2PCallRequest.class.getName());
        if (null == p2PCallRequest)
            return;

        p2PCallRequest.getWaitMsg().clear();
        terminalService.delWaitMsg(P2PCallRequest.class.getName());
        LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"50024 : p2p call failed!");
        p2PCallRequest.makeErrorResponseMsg(ConfInterfaceResult.P2PCALL.getCode(), HttpStatus.OK, ConfInterfaceResult.P2PCALL.getMessage());

        terminalService.cancelCallMt(terminalService);

        terminalService.setDualStream(false);
        freeVmt(terminalService.getE164());

        //清理数据库中的正向通道资源,反向资源在OnMediaCleaned函数中清除
        TerminalMediaResource oldTerminalMediaResource = terminalMediaSourceService.getTerminalMediaResource(terminalService.getE164());
        oldTerminalMediaResource.getForwardResources().clear();
        terminalMediaSourceService.setTerminalMediaResource(oldTerminalMediaResource);
    }

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    private H323ProtocalConfig protocalConfig;

    @Autowired
    private TerminalMediaSourceService terminalMediaSourceService;

    @Autowired
    private ConfInterfacePublishService confInterfacePublishService;

    @Autowired
    private UnifiedDevicePushService unifiedDevicePushService;

}
