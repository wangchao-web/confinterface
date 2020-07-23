package com.kedacom.confinterface.sip;

import com.kedacom.confadapter.IConferenceEventHandler;
import com.kedacom.confadapter.common.ConferenceInfo;
import com.kedacom.confadapter.common.ConferencePresentParticipant;
import com.kedacom.confadapter.media.MediaDescription;
import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dto.MediaResource;
import com.kedacom.confinterface.dto.P2PCallRequest;
import com.kedacom.confinterface.dto.TerminalMediaResource;
import com.kedacom.confinterface.inner.DetailMediaResouce;
import com.kedacom.confinterface.inner.TerminalOnlineStatusEnum;
import com.kedacom.confinterface.service.TerminalManageService;
import com.kedacom.confinterface.service.TerminalMediaSourceService;
import com.kedacom.confinterface.service.TerminalService;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Vector;

public class SipTerminalManageService extends TerminalManageService implements IConferenceEventHandler {

    public SipTerminalManageService(SipProtocalConfig sipProtocalConfig, TerminalMediaSourceService terminalMediaSourceService) {
        super();
        this.sipProtocalConfig = sipProtocalConfig;
        super.terminalMediaSourceService = terminalMediaSourceService;
        setSupportAliasCall(sipProtocalConfig.isSupportAliasCall());
    }

    public SipProtocalConfig getSipProtocalConfig() {
        return sipProtocalConfig;
    }

    @Override
    public void StartUp() {
    }

    @Override
    public TerminalService createTerminal(String e164, boolean bVmt) {
        StringBuffer name = new StringBuffer();

        if (bVmt) {
            name.append(sipProtocalConfig.getBaseSysConfig().getVmtNamePrefix());
            name.append(e164);
        } else {
            name.append("sipmt_");
            name.append(e164);
        }

        SipTerminalService sipTerminalService = new SipTerminalService(e164, name.toString(), bVmt, sipProtocalConfig);
        createConfParticipant(sipTerminalService);

        if (bVmt) {
            freeVmtServiceMap.put(e164, sipTerminalService);
        }
        return sipTerminalService;
    }

    @Override
    @Async("confTaskExecutor")
    public void OnInvited(String participantid, ConferenceInfo conferenceInfo) {
        //虚拟终端被叫时，进入该逻辑
        //有可能被mcu主叫，也有可能是会议终端点对点主叫
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, OnInvited, participantid : " + participantid + ", confId : " + conferenceInfo.getId() + ", threadName:" + Thread.currentThread().getName());
        System.out.println("SIP, OnInvited, participantid : " + participantid + ", confId : " + conferenceInfo.getId() + ", threadName:" + Thread.currentThread().getName());

        processInvitedMsg(participantid, conferenceInfo);
    }

    @Override
    @Async("confTaskExecutor")
    public void OnKickedOff(String participantid) {
        //在呼叫挂断时，会进入该逻辑
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, OnKickedOff, terminal: " + participantid + " is kicked off conference, threadName: " + Thread.currentThread().getName());
        System.out.println("SIP, OnKickedOff, terminal: " + participantid + " is kicked off conference,threadName:" + Thread.currentThread().getName());
        processKickedOffMsg(participantid);
    }

    @Override
    @Async("confTaskExecutor")
    public void OnLocalMediaRequested(String participantid, Vector<MediaDescription> mediaDescriptions) {
        //虚拟终端为被叫时，进入该逻辑
        //mcu发起的呼叫，mediaDescriptions不包含任何东西，需要虚拟终端先提供媒体能力及网络参数
        //会议终端发起的呼叫，mediaDescriptions包含主流视频、音频以及双流的视频、音频
        //该逻辑中需要向流媒体一次性请求主流双流的音视频资源，而且是sendrecv模式
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, OnLocalMediaRequested, request terminal: " + participantid + " local media! threadName:" + Thread.currentThread().getName());
        System.out.println("SIP, OnLocalMediaRequested, request terminal: " + participantid + " local media! threadName:" + Thread.currentThread().getName());

        TerminalService terminalService = usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, OnLocalMediaRequested, not found terminal : " + participantid);
            System.out.println("SIP, OnLocalMediaRequested, not found terminal : " + participantid);
            return;
        }

        if (null == mediaDescriptions) {
            //说明是mcu使用sip协议呼叫虚拟终端,此时需要使用配置文件中的音视频格式做为媒体格式，向流媒体发起资源节点请求
            //需要考虑是否支持双流并配置双流的音视频格式
            mediaDescriptions = new Vector<>();
        }

        if (mediaDescriptions.isEmpty()) {
            //使用配置文件中配置的音视频格式填充mediaDescriptions
            constructMediaDescriptions(sipProtocalConfig.getBaseSysConfig().getVideoCapSetList(),
                    sipProtocalConfig.getBaseSysConfig().getAudioCapSetList(), mediaDescriptions);
        }

        boolean bOK = terminalService.onOpenLogicalChannel(mediaDescriptions);
        if (!bOK) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, OnLocalMediaRequested, onOpenLogicalChannel failed! participantid :" + participantid);
            System.out.println("SIP, OnLocalMediaRequested, onOpenLogicalChannel failed! participantid :" + participantid);

            //处理被叫失败
            if (null != terminalService.getProxyMTE164()){
                P2PCallRequestFail(terminalService);
            }
        }

        //更新redis中的资源信息
        //sip是双向通道，使用forwardchannel进行保存
        synchronized (terminalService) {
            TerminalMediaResource oldTerminalMediaResource = terminalMediaSourceService.getTerminalMediaResource(participantid);
            List<MediaResource> forwardResources = TerminalMediaResource.convertToMediaResource(terminalService.getForwardChannel(), "all");

            boolean bNeedUpdate;
            if (null == oldTerminalMediaResource) {
                bNeedUpdate = true;
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, OnLocalMediaRequested, null == oldTerminalMediaResource, need update! E164:" + participantid + ", forwardResources:" + forwardResources);
                System.out.println("SIP, OnLocalMediaRequested, null == oldTerminalMediaResource, need update! E164:" + participantid + ", forwardResources:" + forwardResources);
            } else {
                List<MediaResource> oldForwardResources = oldTerminalMediaResource.getForwardResources();
                bNeedUpdate = NeedResourcesUpdate(oldForwardResources, forwardResources);
            }

            if (bNeedUpdate) {
                TerminalMediaResource terminalMediaResource = new TerminalMediaResource();
                terminalMediaResource.setMtE164(participantid);
                terminalMediaResource.setForwardResources(forwardResources);
                terminalMediaSourceService.setTerminalMediaResource(terminalMediaResource);
            }
        }
    }

    @Override
    @Async("confTaskExecutor")
    public void OnParticipantLeft(String participantid, ConferencePresentParticipant conferencePresentParticipant) {
        //mcu会议中有与会者离开会场时，进入该逻辑
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, OnParticipantLeft, terminal: " + conferencePresentParticipant.getId() + "left conference! confId: "
                + conferencePresentParticipant.getConf().getId() + ", threadName:" + Thread.currentThread().getName());
        System.out.println("SIP, OnParticipantLeft, terminal: " + conferencePresentParticipant.getId() + "left conference! confId: "
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
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, OnMediaCleaned, terminal: " + participantid + ", media cleaned, threadName: " + Thread.currentThread().getName());
        System.out.println("SIP, OnMediaCleaned, terminal: " + participantid + ", media cleaned, threadName: " + Thread.currentThread().getName());
    }

    @Override
    @Async("confTaskExecutor")
    public void OnRemoteMediaReponsed(String participantid, Vector<MediaDescription> mediaDescriptions) {
        //虚拟终端主动呼叫会议终端或者mcu触发的主叫，mediaDescriptions包含对方主流和双流的音视频信息
        //会议终端主叫时，mediaDescriptions里面不包含任何东西
        //需要向流媒体请求更新

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, OnRemoteMediaReponsed, request terminal: " + participantid + ", threadName:" + Thread.currentThread().getName());
        System.out.println("SIP, OnRemoteMediaReponsed, request terminal: " + participantid + ", threadName:" + Thread.currentThread().getName());

        SipTerminalService terminalService = (SipTerminalService) usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, OnRemoteMediaReponsed�� not found terminal : " + participantid);
            System.out.println("SIP, OnRemoteMediaReponsed�� not found terminal : " + participantid);
            return;
        }

        if (null == mediaDescriptions || mediaDescriptions.isEmpty()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, OnRemoteMediaReponsed, request terminal: " + participantid + " no mediaDescriptions!");
            System.out.println("SIP, OnRemoteMediaReponsed, request terminal: " + participantid + "  no mediaDescriptions!");

            if (null != terminalService.getProxyMTE164()) {
                //处理被叫
                ProcessProxyCall(terminalService);
            }

            return;
        }

        boolean bOk = terminalService.updateExchange(mediaDescriptions);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "SIP, OnRemoteMediaReponsed, updateExchange result : " + bOk);
        System.out.println("SIP, OnRemoteMediaReponsed, updateExchange result : " + bOk);

        if (null != terminalService.getRemoteMtAccount()) {
            if (!bOk) {
                P2PCallRequestFail(terminalService);
            } else {
                for (MediaDescription mediaDescription : mediaDescriptions) {
                    P2PCallRequestSuccess(terminalService, mediaDescription.getStreamIndex());
                }
            }
        }
    }

    @Override
    @Async("confTaskExecutor")
    public void OnKeyFrameRequested(String participantid, Vector<MediaDescription> mediaDescriptions) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Sip, OnKeyFrameRequested, terminal: " + participantid + " is on key frameRequest, threadName: " + Thread.currentThread().getName() + "mediaDescriptions : " + mediaDescriptions.toString());
        System.out.println("Sip, OnKeyFrameRequested, terminal: " + participantid + " is on key frameRequest  conference,threadName:" + Thread.currentThread().getName() + "mediaDescriptions : " + mediaDescriptions.toString());
        ProcessKeyFrameRequested(participantid, mediaDescriptions);
    }

    private void ProcessProxyCall(TerminalService terminalService) {
        P2PCallRequest p2PCallRequest = (P2PCallRequest) terminalService.getWaitMsg(P2PCallRequest.class.getName());
        if (null == p2PCallRequest) {
            System.out.println("Sip, ProcessProxyCall, no p2pCallRequest need deal!");
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
    public SipProtocalConfig getProtocalConfig() {
        return sipProtocalConfig;
    }

    private SipProtocalConfig sipProtocalConfig;
}
