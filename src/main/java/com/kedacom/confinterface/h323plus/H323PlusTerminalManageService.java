package com.kedacom.confinterface.h323plus;

import com.kedacom.confadapter.IConferenceEventHandler;
import com.kedacom.confadapter.common.ConferenceInfo;
import com.kedacom.confadapter.common.ConferencePresentParticipant;
import com.kedacom.confadapter.common.NetAddress;
import com.kedacom.confadapter.media.MediaDescription;
import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dao.H323PlusEnum;
import com.kedacom.confinterface.dto.MediaResource;
import com.kedacom.confinterface.dto.P2PCallRequest;
import com.kedacom.confinterface.inner.DetailMediaResouce;
import com.kedacom.confinterface.inner.TerminalOnlineStatusEnum;
import com.kedacom.confinterface.service.TerminalManageService;
import com.kedacom.confinterface.service.TerminalMediaSourceService;
import com.kedacom.confinterface.service.TerminalService;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class H323PlusRegisterGkHandler implements Runnable {


    public void setH323PlusTerminalService(H323PlusTerminalService h323PlusTerminalService) {
        this.h323PlusTerminalService = h323PlusTerminalService;
    }

    public static List<Future<Boolean>> getFutureList() {
        return futureList;
    }

    private H323PlusTerminalService h323PlusTerminalService;

    private static List<Future<Boolean>> futureList = Collections.synchronizedList(new ArrayList<>());

    @Override
    public synchronized void run() {
        Future<Boolean> H323PlusRegFuture = h323PlusTerminalService.startRegGK();
        try {
            if (null != H323PlusRegFuture && H323PlusRegFuture.get()) {
                futureList.add(H323PlusRegFuture);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


public class H323PlusTerminalManageService extends TerminalManageService implements IConferenceEventHandler {

    public H323PlusTerminalManageService(H323PlusProtocalConfig h323PlusProtocalConfig, TerminalMediaSourceService terminalMediaSourceService) {
        super();
        this.h323PlusProtocalConfig = h323PlusProtocalConfig;
        super.terminalMediaSourceService = terminalMediaSourceService;
        setSupportAliasCall(h323PlusProtocalConfig.isUseGK());
    }

    @Override
    public void StartUp() {
        //如果需要注册GK，则在此处完成所有虚拟终端的注册
        if (!h323PlusProtocalConfig.isUseGK()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[H323PlusTerminalManageService] no need register gk");
            System.out.println("[H323PlusTerminalManageService] no need register gk");
            return;
        }

        int registerGkTerminalNum = freeVmtServiceMap.size() + usedVmtServiceMap.size();
        ExecutorService queueThreadPool = Executors.newFixedThreadPool(h323PlusProtocalConfig.getRegisterGkThreadNum());
        for (TerminalService h323VmtService : freeVmtServiceMap.values()) {
            H323PlusRegisterGkHandler registerGkHandler = new H323PlusRegisterGkHandler();
            registerGkHandler.setH323PlusTerminalService((H323PlusTerminalService) h323VmtService);
            queueThreadPool.execute(registerGkHandler);
        }

        //会议接入微服务异常重启后的Gk注册会涉及到usedVmtServiceMap
        for (TerminalService h323UsedVmtService : usedVmtServiceMap.values()) {
            H323PlusRegisterGkHandler registerGkHandler = new H323PlusRegisterGkHandler();
            registerGkHandler.setH323PlusTerminalService((H323PlusTerminalService) h323UsedVmtService);
            queueThreadPool.execute(registerGkHandler);
        }

        queueThreadPool.shutdown();

        List<Future<Boolean>> futureList = H323PlusRegisterGkHandler.getFutureList();
        while (true) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323PlusTerminalManageService futureList size : " + futureList.size() + ", freeVmt : " + registerGkTerminalNum);
            System.out.println("H323PlusTerminalManageService futureList size : " + futureList.size() + ", freeVmt : " + registerGkTerminalNum);
            if (futureList.size() == registerGkTerminalNum) {
                break;
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {

            }
        }
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "[H323PlusVmtManageService] finished start up, time : " + System.currentTimeMillis());
        System.out.println("[H323PlusVmtManageService] finished start up, time : " + System.currentTimeMillis());
    }

    @Override
    public TerminalService createTerminal(String e164, boolean bVmt) {
        StringBuffer name = new StringBuffer();

        if (bVmt) {
            name.append(h323PlusProtocalConfig.getBaseSysConfig().getVmtNamePrefix());
            name.append(e164);
        } else {
            name.append("h323Plus_");
            name.append(e164);
        }

        H323PlusTerminalService h323PlusTerminalService = new H323PlusTerminalService(e164, name.toString(), bVmt, h323PlusProtocalConfig);
        createConfParticipant(h323PlusTerminalService);

        if (bVmt) {
            freeVmtServiceMap.put(e164, h323PlusTerminalService);
        }
        return h323PlusTerminalService;
    }

    @Override
    @Async("confTaskExecutor")
    public void OnInvited(String participantid, ConferenceInfo conferenceInfo) {
        //虚拟终端被叫时，进入该逻辑
        //有可能被mcu主叫，也有可能是会议终端点对点主叫
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnInvited, participantid : " + participantid + ", confId : " + conferenceInfo.getId() + ", threadName:" + Thread.currentThread().getName());
        System.out.println("H323Plus, OnInvited, participantid : " + participantid + ", confId : " + conferenceInfo.getId() + ", threadName:" + Thread.currentThread().getName());

        processInvitedMsg(participantid, conferenceInfo);
    }

    @Override
    @Async("confTaskExecutor")
    public void OnKickedOff(String participantid) {
        //在呼叫挂断时，会进入该逻辑
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnKickedOff, terminal: " + participantid + " is kicked off conference, threadName: " + Thread.currentThread().getName());
        System.out.println("H323Plus, OnKickedOff, terminal: " + participantid + " is kicked off conference,threadName:" + Thread.currentThread().getName());
        processKickedOffMsg(participantid);
    }

    @Override
    @Async("confTaskExecutor")
    public void OnLocalMediaRequested(String participantid, Vector<MediaDescription> mediaDescriptions) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnLocalMediaRequested, request terminal: " + participantid + ", threadName:" + Thread.currentThread().getName() + ", mediaDescriptions : " + mediaDescriptions.get(0));
        System.out.println("H323Plus, OnLocalMediaRequested, request terminal: " + participantid + ", threadName:" + Thread.currentThread().getName() + ", mediaDescriptions : " + mediaDescriptions.get(0));

        H323PlusTerminalService terminalService = (H323PlusTerminalService) usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnLocalMediaRequested not found terminal : " + participantid);
            System.out.println("H323Plus, OnLocalMediaRequested not found terminal : " + participantid);
            return;
        }

        if (null == mediaDescriptions || mediaDescriptions.isEmpty()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnLocalMediaRequested, request terminal: " + participantid + " no mediaDescriptions!");
            System.out.println("H323Plus, OnLocalMediaRequested, request terminal: " + participantid + "  no mediaDescriptions!");

            if (null != terminalService.getProxyMTE164()) {
                //处理被叫
                ProcessProxyCall(terminalService);
            }

            return;
        }
        String rtcpProtocolType = TerminalService.getIpProtocolType(mediaDescriptions.get(0).getRtcpAddress().getIP());
        if("IP4".equals(rtcpProtocolType)){
            NetAddress netAddress = new NetAddress();
            netAddress.setIP("0.0.0.0");
            netAddress.setPort(mediaDescriptions.get(0).getRtpAddress().getPort());
            mediaDescriptions.get(0).setRtpAddress(netAddress);
        }
        if("IP6".equals(rtcpProtocolType)){
            NetAddress netAddress = new NetAddress();
            netAddress.setIP("::");
            netAddress.setPort(mediaDescriptions.get(0).getRtpAddress().getPort());
            mediaDescriptions.get(0).setRtpAddress(netAddress);
        }
        synchronized (terminalService) {
            H323PlusEnum h323PlusEnum = terminalService.h323PlusupdateExchange(mediaDescriptions);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnLocalMediaRequested, updateExchange result : " + h323PlusEnum.getTypeName());
            System.out.println("H323Plus, OnLocalMediaRequested, updateExchange result : " + h323PlusEnum.getTypeName());

            if (null != terminalService.getRemoteMtAccount()) {
                if (h323PlusEnum == H323PlusEnum.FAILED) {
                    P2PCallRequestFail(terminalService);
                }
                if (h323PlusEnum == H323PlusEnum.SUCCESS) {
                    for (MediaDescription mediaDescription : mediaDescriptions) {
                        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnLocalMediaRequested, updateExchange success P2PCallRequestSuccess ");
                        System.out.println("H323Plus, OnLocalMediaRequested, updateExchange success P2PCallRequestSuccess ");
                        P2PCallRequestSuccess(terminalService, mediaDescription.getStreamIndex(), mediaDescription.getMediaType());
                    }
                }
            }
        }

    }

    @Override
    @Async("confTaskExecutor")
    public void OnParticipantLeft(String participantid, ConferencePresentParticipant conferencePresentParticipant) {
        //mcu会议中有与会者离开会场时，进入该逻辑
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnParticipantLeft, terminal: " + conferencePresentParticipant.getId() + "left conference! confId: "
                + conferencePresentParticipant.getConf().getId() + ", threadName:" + Thread.currentThread().getName());
        System.out.println("H323Plus, OnParticipantLeft, terminal: " + conferencePresentParticipant.getId() + "left conference! confId: "
                + conferencePresentParticipant.getConf().getId() + ", threadName:" + Thread.currentThread().getName());

        processParticipantLeftMsg(participantid, conferencePresentParticipant);
    }

    @Override
    @Async("confTaskExecutor")
    public void OnParticipantJoined(String participantid, ConferencePresentParticipant conferencePresentParticipant) {
        //mcu会议中有新与会者加入时，进入该逻辑
    }

    @Override
    @Async("confTaskExecutor")
    public void OnMediaCleaned(String participantid, Vector<MediaDescription> mediaDescriptions) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnMediaCleaned, terminal: " + participantid + ", media cleaned, threadName: " + Thread.currentThread().getName());
        System.out.println("H323Plus, OnMediaCleaned, terminal: " + participantid + ", media cleaned, threadName: " + Thread.currentThread().getName());
    }


    @Override
    @Async("confTaskExecutor")
    public void OnRemoteMediaReponsed(String participantid, Vector<MediaDescription> mediaDescriptions) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnRemoteMediaReponsed, request terminal: " + participantid + ", threadName:" + Thread.currentThread().getName() + ", mediaDescriptions : " + mediaDescriptions.get(0));
        System.out.println("H323Plus, OnRemoteMediaReponsed, request terminal: " + participantid + ", threadName:" + Thread.currentThread().getName() + ", mediaDescriptions : " + mediaDescriptions.get(0));

        H323PlusTerminalService terminalService = (H323PlusTerminalService) usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnRemoteMediaReponsed not found terminal : " + participantid);
            System.out.println("H323Plus, OnRemoteMediaReponsed not found terminal : " + participantid);
            return;
        }

        if (null == mediaDescriptions || mediaDescriptions.isEmpty()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnRemoteMediaReponsed, request terminal: " + participantid + " no mediaDescriptions!");
            System.out.println("H323Plus, OnRemoteMediaReponsed, request terminal: " + participantid + "  no mediaDescriptions!");

            if (null != terminalService.getProxyMTE164()) {
                //处理被叫
                ProcessProxyCall(terminalService);
            }

            return;
        }

        synchronized (terminalService) {
            H323PlusEnum h323PlusEnum = terminalService.h323PlusupdateExchange(mediaDescriptions);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnRemoteMediaReponsed, updateExchange result : " + h323PlusEnum.getTypeName());
            System.out.println("H323Plus, OnRemoteMediaReponsed, updateExchange result : " + h323PlusEnum.getTypeName());

            if (null != terminalService.getRemoteMtAccount()) {
                if (h323PlusEnum == H323PlusEnum.FAILED) {
                    P2PCallRequestFail(terminalService);
                }
                if (h323PlusEnum == H323PlusEnum.SUCCESS) {
                    for (MediaDescription mediaDescription : mediaDescriptions) {
                        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnRemoteMediaReponsed, updateExchange success P2PCallRequestSuccess ");
                        System.out.println("H323Plus, OnRemoteMediaReponsed, updateExchange success P2PCallRequestSuccess ");
                        P2PCallRequestSuccess(terminalService, mediaDescription.getStreamIndex(), mediaDescription.getMediaType());
                    }
                }
            }
        }

    }

    @Override
    @Async("confTaskExecutor")
    public void OnKeyFrameRequested(String participantid, Vector<MediaDescription> mediaDescriptions) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323Plus, OnKeyFrameRequested, terminal: " + participantid + " is on key frameRequest, threadName: " + Thread.currentThread().getName() + "mediaDescriptions : " + mediaDescriptions.toString());
        System.out.println("H323Plus, OnKeyFrameRequested, terminal: " + participantid + " is on key frameRequest  conference,threadName:" + Thread.currentThread().getName() + "mediaDescriptions : " + mediaDescriptions.toString());
        ProcessKeyFrameRequested(participantid, mediaDescriptions);
    }

    @Override
    public void OnReadyToPrepareLocalChannel(String s, Vector<MediaDescription> vector) {

    }

    private void ProcessProxyCall(TerminalService terminalService) {
        P2PCallRequest p2PCallRequest = (P2PCallRequest) terminalService.getWaitMsg(P2PCallRequest.class.getName());
        if (null == p2PCallRequest) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "H323plus, ProcessProxyCall, no p2pCallRequest need deal!");
            System.out.println("H323plus, ProcessProxyCall, no p2pCallRequest need deal!");
            return;
        }

        List<DetailMediaResouce> mediaResources = terminalService.getForwardChannel();
        for (DetailMediaResouce detailMediaResouce : mediaResources) {
            MediaResource mediaResource = new MediaResource();
            detailMediaResouce.convertTo(mediaResource);

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ProcessProxyCall, add forward mediaResource :" + mediaResource.getId());
            System.out.println("ProcessProxyCall add forward mediaResource :" + mediaResource.getId());
            p2PCallRequest.addForwardResource(mediaResource);
            p2PCallRequest.addReverseResource(mediaResource);
        }

        synchronized (this) {
            p2PCallRequest.getWaitMsg().clear();
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequestSuccess, pubulish terminal status, account:" + p2PCallRequest.getAccount() + ", groupId: " + p2PCallRequest.getGroupId() + ", forwardResources: " + p2PCallRequest.getForwardResources().toString() + ", reverseResources: " + p2PCallRequest.getReverseResources().toString());
        System.out.println("P2PCallRequestSuccess, pubulish terminal status, account: " + p2PCallRequest.getAccount() + ", groupId : " + p2PCallRequest.getGroupId() + ", forwardResources: " + p2PCallRequest.getForwardResources().toString() + ", reverseResources: " + p2PCallRequest.getReverseResources().toString());
        TerminalManageService.publishStatus(p2PCallRequest.getAccount(), p2PCallRequest.getGroupId(), TerminalOnlineStatusEnum.ONLINE.getCode(), p2PCallRequest.getForwardResources(), p2PCallRequest.getReverseResources());

        terminalService.delWaitMsg(P2PCallRequest.class.getName());
    }

    public H323PlusProtocalConfig getH323PlusProtocalConfig() {
        return h323PlusProtocalConfig;
    }

    public void setH323PlusProtocalConfig(H323PlusProtocalConfig h323PlusProtocalConfig) {
        this.h323PlusProtocalConfig = h323PlusProtocalConfig;
    }

    private H323PlusProtocalConfig h323PlusProtocalConfig;
}
