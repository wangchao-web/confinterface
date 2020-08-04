package com.kedacom.confinterface.h323;


import com.kedacom.confadapter.IConferenceEventHandler;
import com.kedacom.confadapter.common.ConferenceInfo;
import com.kedacom.confadapter.common.ConferencePresentParticipant;
import com.kedacom.confadapter.media.MediaDescription;
import com.kedacom.confadapter.media.MediaDirectionEnum;
import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dto.*;
import com.kedacom.confinterface.inner.*;
import com.kedacom.confinterface.service.*;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.slf4j.LoggerFactory;
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

    public H323TerminalManageService(H323ProtocalConfig h323ProtocalConfig, TerminalMediaSourceService terminalMediaSourceService) {
        super();
        this.protocalConfig = h323ProtocalConfig;
        super.terminalMediaSourceService = terminalMediaSourceService;
        setSupportAliasCall(h323ProtocalConfig.isUseGK());
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
        if (bVmt) {
            freeVmtServiceMap.put(e164, h323TerminalService);
        }

        return h323TerminalService;
    }

    @Override
    public void StartUp() {
        //如果需要注册GK，则在此处完成所有虚拟终端的注册
        if (!protocalConfig.isUseGK()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[H323VmtManageService] no need register gk");
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
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "futureList size : " + futureList.size() + ", freeVmt : " + registerGkTerminalNum);
            System.out.println("futureList size : " + futureList.size() + ", freeVmt : " + registerGkTerminalNum);
            if (futureList.size() == registerGkTerminalNum) {
                break;
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {

            }
        }
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[H323VmtManageService] finished start up, time : " + System.currentTimeMillis());
        System.out.println("[H323VmtManageService] finished start up, time : " + System.currentTimeMillis());
    }

    @Override
    @Async("confTaskExecutor")
    public void OnInvited(String participantid, ConferenceInfo conferenceInfo) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnInvited, participantid : " + participantid + ", confId : " + conferenceInfo.getId() + ", threadName:" + Thread.currentThread().getName());
        System.out.println("H323, OnInvited, participantid : " + participantid + ", confId : " + conferenceInfo.getId() + ", threadName:" + Thread.currentThread().getName());

        TerminalService terminalService = processInvitedMsg(participantid, conferenceInfo);
        if (null != terminalService) {
            terminalService.allowExtensiveStream();
        }

        return;
    }

    @Override
    @Async("confTaskExecutor")
    public void OnParticipantJoined(String participantid, ConferencePresentParticipant conferencePresentParticipant) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnParticipantJoined, terminal: " + conferencePresentParticipant.getId() + "join conference! confId : "
                + conferencePresentParticipant.getConf().getId() + ", threadName:" + Thread.currentThread().getName());
        System.out.println("H323, OnParticipantJoined, terminal: " + conferencePresentParticipant.getId() + "join conference! confId : "
                + conferencePresentParticipant.getConf().getId() + ", threadName:" + Thread.currentThread().getName());
    }

    @Override
    @Async("confTaskExecutor")
    public void OnParticipantLeft(String participantid, ConferencePresentParticipant conferencePresentParticipant) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnParticipantLeft, terminal: " + conferencePresentParticipant.getId() + "left conference! confId: "
                + conferencePresentParticipant.getConf().getId() + ", threadName:" + Thread.currentThread().getName());
        System.out.println("H323, OnParticipantLeft, terminal: " + conferencePresentParticipant.getId() + "left conference! confId: "
                + conferencePresentParticipant.getConf().getId() + ", threadName:" + Thread.currentThread().getName());
        processParticipantLeftMsg(participantid, conferencePresentParticipant);
    }

    @Override
    @Async("confTaskExecutor")
    public void OnKickedOff(String participantid) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnKickedOff, terminal: " + participantid + " is kicked off conference, threadName: " + Thread.currentThread().getName());
        System.out.println("H323, OnKickedOff, terminal: " + participantid + " is kicked off conference,threadName:" + Thread.currentThread().getName());
        processKickedOffMsg(participantid);
    }

    @Override
    @Async("confTaskExecutor")
    public void OnLocalMediaRequested(String participantid, Vector<MediaDescription> mediaDescriptions) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnLocalMediaRequested, request terminal: " + participantid + " local media! threadName:" + Thread.currentThread().getName());
        System.out.println("H323, OnLocalMediaRequested, request terminal: " + participantid + " local media! threadName:" + Thread.currentThread().getName());
        H323TerminalService terminalService = (H323TerminalService) usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnLocalMediaRequested, not found terminal : " + participantid);
            System.out.println("H323, OnLocalMediaRequested, not found terminal : " + participantid);
            return;
        }
        Boolean bOK = false;
        synchronized (terminalService) {
            if (protocalConfig.getBaseSysConfig().isSendRecvPort()) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnLocalMediaRequested, open sendRecv port same ");
                System.out.println("H323, OnLocalMediaRequested, open sendRecv port same ");
                mediaDescriptions.get(0).setDirection(MediaDirectionEnum.SendRecv);
            }
            bOK = terminalService.onOpenLogicalChannel(mediaDescriptions);
        }

        if (!bOK) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnLocalMediaRequested, onOpenLogicalChannel failed! participantid :" + participantid);
            System.out.println("H323, OnLocalMediaRequested, onOpenLogicalChannel failed! participantid :" + participantid);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnLocalMediaRequested Failed to create resource node, cancel terminal call! participantid :" + participantid);
            System.out.println("H323, Failed to create resource node, cancel terminal call! participantid :" + participantid);

            if (null != terminalService.getRemoteMtAccount() || null != terminalService.getProxyMTE164()) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnLocalMediaRequested P2PCallRequestFail, cancel terminal call! participantid :" + participantid);
                System.out.println("H323, OnLocalMediaRequested P2PCallRequestFail, cancel terminal call! participantid :" + participantid);
                P2PCallRequestFail(terminalService);
            }
        } else {
            if (terminalService.isReadyToPrepare() && !protocalConfig.getBaseSysConfig().isSendRecvPort()) {
                if (!mediaDescriptions.get(0).getDual()) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnLocalMediaRequested, remoteMtAccount : " + terminalService.getRemoteMtAccount());
                    System.out.println("H323, OnLocalMediaRequested, remoteMtAccount : " + terminalService.getRemoteMtAccount());
                    if (null != terminalService.getRemoteMtAccount() || null != terminalService.getProxyMTE164()) {
                        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnLocalMediaRequested, P2PCallRequestSuccess : ");
                        System.out.println("H323, OnLocalMediaRequested, P2PCallRequestSuccess : ");
                        synchronized (this) {
                            P2PCallRequestSuccess(terminalService, mediaDescriptions.get(0).getStreamIndex(), mediaDescriptions.get(0).getMediaType());
                        }

                    }

                    if (terminalService.getForwardGenericStreamNum().decrementAndGet() != 0) {
                        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnLocalMediaRequested, decrementAndGet : ");
                        System.out.println("H323, OnLocalMediaRequested, decrementAndGet : ");
                    }
                }
            }
        }
        //将虚拟终端的资源更新到数据库中
        synchronized (terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnLocalMediaRequested TerminalMediaResource");
            System.out.println("H323, OnLocalMediaRequested update TerminalMediaResource");
            TerminalMediaResource oldTerminalMediaResource = terminalMediaSourceService.getTerminalMediaResource(participantid);
            List<MediaResource> forwardResources = TerminalMediaResource.convertToMediaResource(terminalService.getForwardChannel(), "all");
            List<MediaResource> reverseResources = TerminalMediaResource.convertToMediaResource(terminalService.getReverseChannel(), "all");
            boolean bNeedUpdate;
            if (null == oldTerminalMediaResource) {
                bNeedUpdate = true;
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnLocalMediaRequested, null == oldTerminalMediaResource, need update! E164:" + participantid + ", forwardResources:" + forwardResources + ", reverseResources:" + reverseResources);
                System.out.println("H323, OnLocalMediaRequested, null == oldTerminalMediaResource, need update! E164:" + participantid + ", forwardResources:" + forwardResources + ", reverseResources:" + reverseResources);
            } else {
                List<MediaResource> oldForwardResources = oldTerminalMediaResource.getForwardResources();
                bNeedUpdate = NeedResourcesUpdate(oldForwardResources, forwardResources);

                List<MediaResource> oldReverseResources = oldTerminalMediaResource.getReverseResources();
                bNeedUpdate |= NeedResourcesUpdate(oldReverseResources, reverseResources);
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
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnRemoteMediaReponsed, request terminal: " + participantid + " local media! threadName:" + Thread.currentThread().getName());
        System.out.println("H323, OnRemoteMediaReponsed, request terminal: " + participantid + " local media! threadName:" + Thread.currentThread().getName());
        H323TerminalService terminalService = (H323TerminalService) usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnRemoteMediaReponsed， not found terminal : " + participantid);
            System.out.println("H323, OnRemoteMediaReponsed， not found terminal : " + participantid);
            return;
        }
        boolean bOk = false;
        synchronized (terminalService) {
            bOk = terminalService.updateExchange(mediaDescriptions);
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnRemoteMediaReponsed, updateExchange result : " + bOk
                + ", streamIndex:" + mediaDescriptions.get(0).getStreamIndex() + ", bDual: " + mediaDescriptions.get(0).getDual());
        System.out.println("H323, OnRemoteMediaReponsed, updateExchange result : " + bOk + ", streamIndex:" + mediaDescriptions.get(0).getStreamIndex() + ", bDual: " + mediaDescriptions.get(0).getDual());
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnRemoteMediaReponsed ReadyToPrepare : " + terminalService.isReadyToPrepare());
        System.out.println("H323, OnRemoteMediaReponsed ReadyToPrepare : " + terminalService.isReadyToPrepare());

        if (bOk) {
            if (!mediaDescriptions.get(0).getDual()) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnRemoteMediaReponsed, remoteMtAccount : " + terminalService.getRemoteMtAccount());
                System.out.println("H323, OnRemoteMediaReponsed, remoteMtAccount : " + terminalService.getRemoteMtAccount());

                if (null != terminalService.getRemoteMtAccount() || null != terminalService.getProxyMTE164()) {
                    synchronized (this) {
                        P2PCallRequestSuccess(terminalService, mediaDescriptions.get(0).getStreamIndex(), mediaDescriptions.get(0).getMediaType());
                    }

                }

                if (terminalService.getForwardGenericStreamNum().decrementAndGet() != 0) {
                    return;
                }

                ResumeDualStream(terminalService);
                return;
            }

            DualStreamRequestSuccess(terminalService, mediaDescriptions.get(0).getStreamIndex());
            return;
        } else {
            //更新资源节点失败时,取消终端呼叫,并通知上层业务呼叫失败
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnRemoteMediaReponsed Failed to update resource node, cancel terminal call status notification! participantid :" + terminalService.getRemoteMtAccount());
            System.out.println("H323, OnRemoteMediaReponsed Failed to update resource node, cancel terminal call status notification! participantid :" + terminalService.getRemoteMtAccount());
        }

        if (mediaDescriptions.get(0).getDual()) {
            DualStreamRequestFail(terminalService);
        } else if (null != terminalService.getRemoteMtAccount() || null != terminalService.getProxyMTE164()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnRemoteMediaReponsed P2PCallRequestFail cancel terminal call status notification! participantid :" + terminalService.getRemoteMtAccount());
            System.out.println("H323, OnRemoteMediaReponsed P2PCallRequestFail  cancel terminal call status notification! participantid :" + terminalService.getRemoteMtAccount());
            P2PCallRequestFail(terminalService);
        }

    }

    @Override
    @Async("confTaskExecutor")
    public void OnMediaCleaned(String participantid, Vector<MediaDescription> mediaDescriptions) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnMediaCleaned, terminal: " + participantid + ", media cleaned, threadName: " + Thread.currentThread().getName());
        System.out.println("H323, OnMediaCleaned, terminal: " + participantid + ", media cleaned, threadName: " + Thread.currentThread().getName());
        H323TerminalService terminalService = (H323TerminalService) usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnMediaCleaned, not found terminal:" + participantid);
            System.out.println("H323, OnMediaCleaned, not found terminal: " + participantid);
            return;
        }

        List<String> resourceIds = new ArrayList<>();
        List<DetailMediaResouce> mediaResouces = terminalService.getReverseChannel();
        if (null == mediaResouces) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnMediaCleaned, terminal(" + participantid + ") has no reverse channel!");
            return;
        }

        for (MediaDescription mediaDescription : mediaDescriptions) {
            if (!mediaDescription.getDual()) {
                for (DetailMediaResouce mediaResouce : mediaResouces) {
                    if (mediaResouce.getStreamIndex() != mediaDescription.getStreamIndex()) {
                        continue;
                    }
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnMediaCleaned, type:" + mediaResouce.getType() + ", resourceId:" + mediaResouce.getId() + ", streamIndex:" + mediaDescription.getStreamIndex());
                    System.out.println("H323, OnMediaCleaned, type:" + mediaResouce.getType() + ", resourceId:" + mediaResouce.getId() + ", streamIndex:" + mediaDescription.getStreamIndex());
                    resourceIds.add(mediaResouce.getId());
                    break;
                }
            } else {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnMediaCleaned, dual stream is closed, publish dual status!");
                System.out.println("H323, OnMediaCleaned, dual stream is closed, publish dual status!");
                terminalService.dualPublish();
            }
        }

        if (resourceIds.isEmpty()) {
            System.out.println("H323, OnMediaCleaned, no resources need release! ");
            return;
        }

        terminalService.removeExchange(resourceIds);

        synchronized (terminalService) {
            //删除对应的资源信息
            Iterator<String> resoureIdIterator = resourceIds.iterator();
            while (resoureIdIterator.hasNext()) {
                String resourceId = resoureIdIterator.next();
                mediaResouces = terminalService.getReverseChannel();
                if (null == mediaResouces) {
                    break;
                }

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

    @Override
    @Async("confTaskExecutor")
    public void OnKeyFrameRequested(String participantid, Vector<MediaDescription> mediaDescriptions) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnKeyFrameRequested, terminal: " + participantid + " is on key frameRequest, threadName: " + Thread.currentThread().getName() + "mediaDescriptions : " + mediaDescriptions.toString());
        System.out.println("H323, OnKeyFrameRequested, terminal: " + participantid + " is on key frameRequest  conference,threadName:" + Thread.currentThread().getName() + "mediaDescriptions : " + mediaDescriptions.toString());
        ProcessKeyFrameRequested(participantid, mediaDescriptions);
    }

    @Override
    @Async("confTaskExecutor")
    public void OnReadyToPrepareLocalChannel(String participantid, Vector<MediaDescription> mediaDescriptions) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnReadyToPrepareLocalChannel, request terminal: " + participantid + " local media! threadName:" + Thread.currentThread().getName());
        System.out.println("H323, OnReadyToPrepareLocalChannel, request terminal: " + participantid + " local media! threadName:" + Thread.currentThread().getName());
        H323TerminalService terminalService = (H323TerminalService) usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnReadyToPrepareLocalChannel, not found terminal : " + participantid);
            System.out.println("H323, OnReadyToPrepareLocalChannel, not found terminal : " + participantid);
            return;
        }
        MediaDescription mediaDescription = mediaDescriptions.get(0);
        mediaDescription.getRtcpAddress().setIP("0.0.0.0");
        mediaDescription.getRtpAddress().setIP("0.0.0.0");
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnReadyToPrepareLocalChannel participantid : " + participantid + " : " + mediaDescription);
        System.out.println("H323, OnReadyToPrepareLocalChannel participantid : " + participantid + " : " + mediaDescription);
        terminalService.setReadyToPrepare(true);
        Boolean bOK = false;
        if(protocalConfig.baseSysConfig.isSendRecvPort()){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnReadyToPrepareLocalChannel participantid : " + participantid + " ,SendRecvPort : " + protocalConfig.baseSysConfig.isSendRecvPort());
            System.out.println("H323, OnReadyToPrepareLocalChannel participantid : " + participantid + " ,SendRecvPort : " + protocalConfig.baseSysConfig.isSendRecvPort());
            return;
        }
        synchronized (terminalService) {
            bOK = terminalService.openLogicalChannel(mediaDescriptions);
        }

        if (!bOK) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnReadyToPrepareLocalChannel, onOpenLogicalChannel failed! participantid :" + participantid);
            System.out.println("H323, OnReadyToPrepareLocalChannel, onOpenLogicalChannel failed! participantid :" + participantid);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, OnReadyToPrepareLocalChannel Failed to create resource node, cancel terminal call! participantid :" + participantid);
            System.out.println("H323, OnReadyToPrepareLocalChannel Failed to create resource node, cancel terminal call! participantid :" + participantid);

            if (null != terminalService.getRemoteMtAccount() || null != terminalService.getProxyMTE164()) {
                P2PCallRequestFail(terminalService);
            }
        }

    }

    private void ResumeDualStream(H323TerminalService terminalService) {
        //如果主流全部开启，判断是否需要恢复辅流
        if (terminalService.getResumeDualStream().compareAndSet(true, false)) {
            boolean bResumeOk = terminalService.resumeDualStream();
            if (bResumeOk) {
                return;
            }

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
            if (detailMediaResouce.getStreamIndex() != dualStreamIndex) {
                continue;
            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323, DualStreamRequestSuccess, dual streamIndex:" + dualStreamIndex);
            System.out.println("H323, DualStreamRequestSuccess, dual streamIndex:" + dualStreamIndex);
            mediaResource.setType(detailMediaResouce.getType());
            mediaResource.setDual(true);
            mediaResource.setId(detailMediaResouce.getId());

            break;
        }

        /*if (null == terminalService.getRemoteMtAccount()) {*/
        //非点对点呼叫
        StartDualStreamRequest startDualStreamRequest = (StartDualStreamRequest) terminalService.getWaitMsg(StartDualStreamRequest.class.getName());
        if (null == startDualStreamRequest) {
            return;
        }

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
        if (null == terminalService.getRemoteMtAccount() && null == terminalService.getProxyMTE164()) {
            waitMsgKey = StartDualStreamRequest.class.getName();
            dualStreamRequest = terminalService.getWaitMsg(waitMsgKey);
        } else {
            waitMsgKey = P2PCallRequest.class.getName();
            dualStreamRequest = terminalService.getWaitMsg(waitMsgKey);
        }

        if (null == dualStreamRequest) {
            return;
        }

        LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "DualStreamRequestFail, 50021 : update exchange node failed!");
        if (null == terminalService.getProxyMTE164()) {
            dualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.UPDATE_EXCHANGENODE_FAILED.getCode(), HttpStatus.OK, ConfInterfaceResult.UPDATE_EXCHANGENODE_FAILED.getMessage());
        }

        terminalService.delWaitMsg(waitMsgKey);

        boolean bOk = terminalService.closeDualStreamChannel();
        if (bOk) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "DualStreamRequestFail, closeDualStreamChannel OK!");
            System.out.println("DualStreamRequestFail, closeDualStreamChannel OK!");
        } else {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "DualStreamRequestFail, closeDualStreamChannel failed!");
            System.out.println("DualStreamRequestFail, closeDualStreamChannel failed!");
        }
    }

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    private H323ProtocalConfig protocalConfig;
}
