package com.kedacom.confinterface.service;

import com.kedacom.confadapter.IConferenceManager;
import com.kedacom.confadapter.ILocalConferenceParticipant;
import com.kedacom.confadapter.common.ConfSessionPeer;
import com.kedacom.confadapter.common.ConferenceInfo;
import com.kedacom.confadapter.common.ConferencePresentParticipant;
import com.kedacom.confadapter.common.ServerErrorEnum;
import com.kedacom.confadapter.media.*;
import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dao.InspectionSrcParam;
import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.dto.MediaResource;
import com.kedacom.confinterface.dto.P2PCallRequest;
import com.kedacom.confinterface.dto.P2PCallResult;
import com.kedacom.confinterface.dto.TerminalMediaResource;
import com.kedacom.confinterface.inner.*;
import com.kedacom.confinterface.restclient.mcu.InspectionStatusEnum;
import com.kedacom.confinterface.syssetting.BaseSysConfig;
import com.kedacom.confinterface.util.AudioCap;
import com.kedacom.confinterface.util.VideoCap;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TerminalManageService {

    public TerminalManageService() {
        super();
        this.freeVmtServiceMap = new ConcurrentHashMap<>();
        this.usedVmtServiceMap = new ConcurrentHashMap<>();
        this.supportAliasCall = false;
    }

    public boolean isSupportAliasCall() {
        return supportAliasCall;
    }

    public void setSupportAliasCall(boolean supportAliasCall) {
        this.supportAliasCall = supportAliasCall;
    }

    public abstract TerminalService createTerminal(String e164, boolean bVmt);

    public abstract void StartUp();

    public TerminalService processInvitedMsg(String participantid, ConferenceInfo conferenceInfo) {
        TerminalService terminalService = usedVmtServiceMap.get(participantid);
        if (null != terminalService) {
            terminalService.setConfId(conferenceInfo.getId());
            return terminalService;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "proccessInvitedMsg, not found participant!!");
        System.out.println("proccessInvitedMsg, not found participant!! in used vmt map");

        terminalService = freeVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "proccessInvitedMsg, not found participant!! in free vmt map!");
            System.out.println("proccessInvitedMsg, not found participant!! in free vmt map!");
            return null;
        }

        if (null == terminalService.getProxyMTE164()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "proccessInvitedMsg, found participant in free vmt map, proxyMt is null, check confEntity!!");
            System.out.println("proccessInvitedMsg, found participant in free vmt map, proxyMt is null, check confEntity!!");

            ConfSessionPeer proxyMT = conferenceInfo.getCallee();
            if ((null == proxyMT.getId() || proxyMT.getId().isEmpty())
                    && (null == proxyMT.getName() || proxyMT.getName().isEmpty())) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "proccessInvitedMsg, confEntity is null, ignore msg!!");
                System.out.println("proccessInvitedMsg, confEntity is null, ignore msg!!");
                return null;
            }

            terminalService.bindProxyMT(proxyMT);
        }

        //走入此处，表明有系统外的设备需要主动呼叫该虚拟终端代理的实体终端
        P2PCallResult p2PCallResult = terminalService.translateCall(participantid, conferenceInfo.getCaller());
        if (null == p2PCallResult) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "proccessInvitedMsg, translateCall, reject invitation!");
            System.out.println("proccessInvitedMsg, translateCall fail, reject invitation!");
            if (terminalService.isDynamicBind()) {
                terminalService.unBindProxyMT();
            }
            terminalService.getConferenceParticipant().AcceptInvitation(false);
            return null;
        }

        String groupId = p2PCallResult.getGroupId();
        conferenceInfo.setId(groupId);

        //将该虚拟终端由空闲队列移入工作队列
        terminalService = getVmt(participantid);

        //创建p2p呼叫组，并将该虚拟终端加入呼叫组
        P2PCallGroup p2PCallGroup = confInterfaceService.getP2pCallGroupMap().computeIfAbsent(groupId, k -> new P2PCallGroup(groupId));
        p2PCallGroup.addCallMember(terminalService.getProxyMTE164(), terminalService);

        //接受本地呼叫请求
        terminalService.acceptInvited(p2PCallResult.getVidoeCodec(), null, p2PCallResult.getDeviceName());

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "proccessInvitedMsg, translateCall Ok, accept invitation!");
        System.out.println("proccessInvitedMsg, translateCall Ok, accept invitation!");

        return terminalService;
    }

    public void processKickedOffMsg(String participantid) {
        TerminalService terminalService = usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "processKickedOffMsg, not found terminal! participantid: " + participantid);
            System.out.println("processKickedOffMsg, not found terminal!");
            return;
        }

        if (terminalService.dualSource.size() > 0) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "processKickedOffMsg dualSource.size() : " + terminalService.dualSource.size());
            System.out.println("processKickedOffMsg dualSource.size() : " + terminalService.dualSource.size());
            terminalService.dualSource.clear();
        }


        String groupId = terminalService.getGroupId();
        Map<String, P2PCallGroup> p2pCallGroupMap = ConfInterfaceService.p2pCallGroupMap;
        if (null != p2pCallGroupMap && null != groupId && p2pCallGroupMap.containsKey(groupId)) {
            P2PCallGroup p2PCallGroup = p2pCallGroupMap.get(groupId);
            String mtAccount = terminalService.getRemoteMtAccount();
            if (null != terminalService.getProxyMTE164()) {
                //如果是被叫，key为代理会议终端的E164号
                mtAccount = terminalService.getProxyMTE164();
                System.out.println("processKickedOffMsg, proxyMt : " + mtAccount);
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "processKickedOffMsg, vmt(" + participantid + ") proxy mt(" + mtAccount + ")");
            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "processKickedOffMsg, mtAccount: " + mtAccount);
            System.out.println("OnKickedOff, mtAccount " + mtAccount);
            if (null != mtAccount) {
                terminalMediaSourceService.delP2PMtMember(groupId, mtAccount);
                terminalMediaSourceService.delP2PVmtMember(groupId, participantid);
                p2PCallGroup.removeCallMember(mtAccount);
                if (p2PCallGroup.getCallMap().isEmpty()) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "processKickedOffMsg, remove p2pCallGroup : " + groupId);
                    p2pCallGroupMap.remove(groupId);
                }
            }

            //被叫时，因为拿不到主叫的E164，因此使用虚拟终端的E164做为标识
            if (null != terminalService.getProxyMTE164()) {
                if (terminalService.isDynamicBind()) {
                    terminalService.unBindProxyMT();
                }
                mtAccount = terminalService.getE164();
            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "processKickedOffMsg, vmt(" + terminalService.getE164() + ") is offline, groupId: " + groupId + ", mtAccount : " + mtAccount);
            System.out.println("processKickedOffMsg, vmt(" + terminalService.getE164() + ") is offline, groupId : " + groupId + ", mtAccount : " + mtAccount);

            if (null != mtAccount) {
                TerminalManageService.publishStatus(mtAccount, groupId, TerminalOnlineStatusEnum.OFFLINE.getCode());
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
        usedVmtServiceMap.remove(participantid);
        freeVmtServiceMap.put(participantid, terminalService);
    }

    public void processParticipantLeftMsg(String participantid, ConferencePresentParticipant conferencePresentParticipant) {
        /*如果下线的是虚拟终端,则不需要额外的处理,因为虚拟终端不会选看或者被其他虚拟终端选看*/
        String offlineMtE164 = conferencePresentParticipant.getId();
        TerminalService offlineTerminal = findVmt(offlineMtE164);
        if (null != offlineTerminal) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "OnParticipantLeft, offline terminal is vmt!!");
            System.out.println("OnParticipantLeft, offline terminal is vmt!!");
            return;
        }

        TerminalService terminalService = usedVmtServiceMap.get(participantid);
        if (terminalService.isInspection()) {
            //判断选看的是否是下线的终端，如果是，则需要将选看状态清除
            InspectionSrcParam inspectionParam = terminalService.getInspectionParam();
            if (inspectionParam.getMtE164().equals(offlineMtE164)) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "OnParticipantLeft, inspection left terminal! dstMt:" + participantid);
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
            if (null == inspectedParam) {
                return;
            }

            terminalService.delInspentedTerminal(offlineMtE164);
        }
    }

    public TerminalService getFreeVmt() {
        synchronized (freeVmtServiceMap) {
            if (freeVmtServiceMap.isEmpty()) {
                return null;
            }

            for (Map.Entry<String, TerminalService> terminalServiceEntry : freeVmtServiceMap.entrySet()) {
                TerminalService terminalService = terminalServiceEntry.getValue();
                freeVmtServiceMap.remove(terminalServiceEntry.getKey());
                usedVmtServiceMap.put(terminalService.getE164(), terminalService);

                return terminalService;
            }

            return null;
        }
    }

    public List<TerminalService> getFreeVmts(int vmtNum) {
        synchronized (freeVmtServiceMap) {
            if (freeVmtServiceMap.isEmpty() || freeVmtServiceMap.size() < vmtNum) {
                return null;
            }

            int chooseVmtNum = 0;
            List<TerminalService> terminals = new ArrayList<>();
            for (Map.Entry<String, TerminalService> terminalServiceEntry : freeVmtServiceMap.entrySet()) {
                TerminalService terminalService = terminalServiceEntry.getValue();
                freeVmtServiceMap.remove(terminalServiceEntry.getKey());
                usedVmtServiceMap.put(terminalService.getE164(), terminalService);
                terminals.add(terminalService);

                chooseVmtNum++;
                if (chooseVmtNum == vmtNum) {
                    break;
                }
            }

            return terminals;
        }
    }

    public TerminalService getVmt(String e164) {
        synchronized (this) {
            TerminalService terminalService = freeVmtServiceMap.get(e164);
            if (null != terminalService) {
                freeVmtServiceMap.remove(e164);
                usedVmtServiceMap.put(e164, terminalService);
                return terminalService;
            }
            return null;
        }
    }

    public TerminalService findVmt(String e164) {
        synchronized (this) {
            TerminalService terminalService = freeVmtServiceMap.get(e164);
            if (null != terminalService) {
                return terminalService;
            }

            terminalService = usedVmtServiceMap.get(e164);
            return terminalService;
        }
    }

    public List<TerminalService> queryAllUsedVmts() {
        if (usedVmtServiceMap.isEmpty()) {
            return null;
        }

        List<TerminalService> terminalServices = new ArrayList<>();
        for (Map.Entry<String, TerminalService> terminalServiceEntry : usedVmtServiceMap.entrySet()) {
            terminalServices.add(terminalServiceEntry.getValue());
        }

        return terminalServices;
    }

    public void freeVmt(String e164) {
        synchronized (usedVmtServiceMap) {
            TerminalService vmtService = usedVmtServiceMap.get(e164);
            if (null == vmtService) {
                return;
            }

            usedVmtServiceMap.remove(e164);
            freeVmtServiceMap.put(e164, vmtService);
        }
    }

    public void freeVmts(List<Terminal> vmts) {
        synchronized (usedVmtServiceMap) {
            for (Terminal vmt : vmts) {
                TerminalService vmtService = usedVmtServiceMap.get(vmt.getMtE164());
                if (null == vmtService) {
                    return;
                }

                usedVmtServiceMap.remove(vmt.getMtE164());
                freeVmtServiceMap.put(vmt.getMtE164(), vmtService);
            }
        }
    }

    public void setConferenceManage(IConferenceManager conferenceManage) {
        this.conferenceManager = conferenceManage;
    }

    public ConfInterfaceService getConfInterfaceService() {
        return confInterfaceService;
    }

    public void setConfInterfaceService(ConfInterfaceService confInterfaceService) {
        this.confInterfaceService = confInterfaceService;
    }

    public IConferenceManager getConferenceManager() {
        return conferenceManager;
    }

    public void createConfParticipant(TerminalService terminalService) {
        if (null == conferenceManager) {
            return;
        }

        if (terminalService.isVmt()) {
            ILocalConferenceParticipant conferenceParticipant = conferenceManager.CreateLocalParticipant(terminalService.getE164());
            terminalService.setConferenceParticipant(conferenceParticipant);
        } else {
            terminalService.setConferenceParticipant(null);
        }
    }

    public static void publishStatus(String account, String groupId, int status, List<MediaResource> forwardResources, List<MediaResource> reverseResources) {
        if (null == confInterfacePublishService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "TerminalManagerService, 1, publishStatus, confInterfacePublishService is null **************");
            System.out.println("TerminalManagerService, 1, publishStatus, confInterfacePublishService is null **************");
            return;
        } else {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "TerminalManagerService, 1, publishStatus, confInterfacePublishService is not null **************");
            System.out.println("TerminalManagerService, 1, publishStatus, confInterfacePublishService is not null **************");
        }
        confInterfacePublishService.publishStatus(account, groupId, status, forwardResources, reverseResources);
    }

    public static void publishStatus(String account, String groupId, int status) {
        if (null == confInterfacePublishService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "TerminalManagerService, 2, publishStatus, confInterfacePublishService is null **************");
            System.out.println("TerminalManagerService, 2, publishStatus, confInterfacePublishService is null **************");
            return;
        } else {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "TerminalManagerService, 2, publishStatus, confInterfacePublishService is not null **************");
            System.out.println("TerminalManagerService, 2, publishStatus, confInterfacePublishService is not null **************");
        }

        confInterfacePublishService.publishStatus(account, groupId, status);
    }

    public static void publishStatus(String account, String groupId, int status, int faileCode) {
        if (null == confInterfacePublishService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "TerminalManagerService, account : " + account + "2, publishStatus, confInterfacePublishService is null **************");
            System.out.println("TerminalManagerService, account : " + account + "2, publishStatus, confInterfacePublishService is null **************");
            return;
        } else {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "TerminalManagerService, account : " + account + " 2, publishStatus, confInterfacePublishService is not null **************");
            System.out.println("TerminalManagerService, account : " + account + " 2, publishStatus, confInterfacePublishService is not null **************");
        }

        confInterfacePublishService.publishStatus(account, groupId, status, faileCode);
    }

    public static void setPublishService(ConfInterfacePublishService inConfInterfacePublishService) {
        confInterfacePublishService = inConfInterfacePublishService;
    }

    //用于会议服务断链再重启之后推送状态
    public static void publishStatus(SubscribeMsgTypeEnum type, String publishUrl, Object publishmsg) {
        confInterfacePublishService.publishStatus(type, publishUrl, publishmsg);
    }

    public int queryFreeVmtServiceMap() {
        return freeVmtServiceMap.size();
    }

    public int queryUsedVmtServiceMap() {
        return usedVmtServiceMap.size();
    }

    protected void P2PCallRequestSuccess(TerminalService terminalService, int streamIndex,String mediaType) {
        P2PCallRequest p2PCallRequest = (P2PCallRequest) terminalService.getWaitMsg(P2PCallRequest.class.getName());
        if (null == p2PCallRequest) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequestSuccess, no p2pCallRequest need deal!");
            System.out.println("P2PCallRequestSuccess, no p2pCallRequest need deal!");
            return;
        }

        List<DetailMediaResouce> mediaResources = terminalService.getForwardChannel();
        for (DetailMediaResouce detailMediaResouce : mediaResources) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequestSuccess, add forward mediaResource :" + detailMediaResouce.toString());
            System.out.println("P2PCallRequestSuccess add forward mediaResource :" + detailMediaResouce.toString());

            if (!detailMediaResouce.getSdp().contains("sendrecv")) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequestSuccess streamIndex" + streamIndex + detailMediaResouce.getStreamIndex());
                System.out.println("P2PCallRequestSuccess streamIndex " + streamIndex + detailMediaResouce.getStreamIndex());
                if (detailMediaResouce.getStreamIndex() != streamIndex) {
                    continue;
                }
            }else{
                if (!mediaType.equals(detailMediaResouce.getType())){
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequestSuccess streamIndex is mediaType :" + mediaType);
                    System.out.println("P2PCallRequestSuccess mediaType is same :" + mediaType );
                    continue;
                }
            }

            MediaResource mediaResource = new MediaResource();
            detailMediaResouce.convertTo(mediaResource);

            p2PCallRequest.addForwardResource(mediaResource);
            synchronized (terminalService) {
                p2PCallRequest.removeMsg(P2PCallRequest.class.getName());
            }

            if (detailMediaResouce.getSdp().contains("sendrecv")) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequestSuccess, sendrecv, add Reverse Resouce!!");
                System.out.println("P2PCallRequestSuccess, sendrecv, add Reverse Resouce!!");
                p2PCallRequest.addReverseResource(mediaResource);
                terminalService.addReverseChannel(detailMediaResouce);  //对于sip来说，正向和反向资源相同
                synchronized (terminalService) {
                    p2PCallRequest.removeMsg(P2PCallRequest.class.getName());
                }
            }

            if (p2PCallRequest.isSuccessResponseMsg()) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequestSuccess, pubulish terminal status, account:" + p2PCallRequest.getAccount() + ", groupId: " + p2PCallRequest.getGroupId() + ", forwardResources: " + p2PCallRequest.getForwardResources().toString() + ", reverseResources: " + p2PCallRequest.getReverseResources().toString());
                System.out.println("P2PCallRequestSuccess, pubulish terminal status, account: " + p2PCallRequest.getAccount() + ", groupId : " + p2PCallRequest.getGroupId() + ", forwardResources: " + p2PCallRequest.getForwardResources().toString() + ", reverseResources: " + p2PCallRequest.getReverseResources().toString());

                TerminalManageService.publishStatus(p2PCallRequest.getAccount(), p2PCallRequest.getGroupId(), TerminalOnlineStatusEnum.ONLINE.getCode(), p2PCallRequest.getForwardResources(), p2PCallRequest.getReverseResources());
            }
            break;
        }

        if (p2PCallRequest.getWaitMsg().isEmpty()) {
            terminalService.delWaitMsg(P2PCallRequest.class.getName());
        }

    }

    protected void P2PCallRequestFail(TerminalService terminalService) {
        P2PCallRequest p2PCallRequest = (P2PCallRequest) terminalService.getWaitMsg(P2PCallRequest.class.getName());
        if (null == p2PCallRequest) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequestFail p2PCallRequest is null *************");
            System.out.println("P2PCallRequestFail p2PCallRequest is null *************");
            return;
        }

        p2PCallRequest.getWaitMsg().clear();
        terminalService.delWaitMsg(P2PCallRequest.class.getName());
        LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequestFail, 50024 : p2p call failed!");
        System.out.println("P2PCallRequestFail, 50024 : p2p call failed!");
        //注释掉得原因：点对点呼叫时，已经在接收到消息时已经回复成功，因此后续不管失败还是成功，不需要再回复消息
        // 只需要将终端状态推送上去即可
//        if (0 != p2PCallRequest.getAccount().compareTo(terminalService.getE164())) {
//            //如果请求消息中的账号与虚拟终端的账号不一致，说明是实际的点对点请求，
//            // 否则则说明是有MT从系统外部主动呼叫虚拟终端
//            p2PCallRequest.makeErrorResponseMsg(ConfInterfaceResult.P2P_CALL.getCode(), HttpStatus.OK, ConfInterfaceResult.P2P_CALL.getMessage());
//        }

        String groupId = terminalService.getGroupId();
        P2PCallGroup p2PCallGroup = ConfInterfaceService.p2pCallGroupMap.get(groupId);
        if (null == p2PCallGroup) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequestFail callGroup is null *************");
            System.out.println("P2PCallRequestFail callGroup is null *************");
            return;
        }

        String remoteMtAccount = terminalService.getRemoteMtAccount() != null ? terminalService.getRemoteMtAccount() : terminalService.getProxyMTE164();
        p2PCallGroup.removeCallMember(remoteMtAccount);
        if (p2PCallGroup.getCallMap().isEmpty()) {
            ConfInterfaceService.p2pCallGroupMap.remove(groupId);
        }

        TerminalManageService.publishStatus(remoteMtAccount, groupId, TerminalOnlineStatusEnum.OFFLINE.getCode(), TerminalOfflineReasonEnum.NmediaResource.getCode());
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequestFail removeCallMember! CallMember :" + remoteMtAccount + ", GroupId " + groupId);
        System.out.println("P2PCallRequestFail removeCallMember! CallMember :" + remoteMtAccount + ", GroupId " + groupId);

        boolean bOk = terminalService.cancelCallMt();
        if (!bOk) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "P2PCallRequestFail, cancelCallMt fail, vmt: " + terminalService.getE164() + ", account: " + p2PCallRequest.getAccount());
            System.out.println("P2PCallRequestFail, cancelCallMt fail, vmt: " + terminalService.getE164() + ", account: " + p2PCallRequest.getAccount());
        }
    }

    protected boolean NeedResourcesUpdate(List<MediaResource> oldResources, List<MediaResource> newResources) {
        if (null == oldResources || oldResources.size() != newResources.size()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "OnLocalMediaRequested, 1. forward resource need update! oldResources:" + oldResources + ", newResource:" + newResources);
            System.out.println("OnLocalMediaRequested, 1. forward resource need update! oldResources:" + oldResources + ", newResource:" + newResources);
            return true;
        }

        oldResources.removeAll(newResources);
        if (!oldResources.isEmpty()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "OnLocalMediaRequested, 2. forward resource need update! oldResources:" + oldResources + ", newResource:" + newResources);
            System.out.println("OnLocalMediaRequested, 2. forward resource need update! oldResources:" + oldResources + ", newResource:" + newResources);
            return true;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "OnLocalMediaRequested, forward resource no need update! Resources:" + newResources);
        System.out.println("OnLocalMediaRequested, forward resource no need update! Resources:" + newResources);

        return false;
    }

    protected void constructMediaDescriptions(List<VideoCap> videoCaps, List<AudioCap> audioCaps, Vector<MediaDescription> mediaDescriptions) {
        int streamIndex = 0;
        if (!videoCaps.isEmpty()) {
            for (VideoCap videoCap : videoCaps) {
                VideoMediaDescription videoMediaDescription = new VideoMediaDescription();
                VideoCap.constructMediaDescription(videoCap, videoMediaDescription);
                videoMediaDescription.setStreamIndex(streamIndex++);
                mediaDescriptions.add(videoMediaDescription);
            }
        }

        if (!audioCaps.isEmpty()) {
            for (AudioCap audioCap : audioCaps) {
                AudioMediaDescription audioMediaDescription = new AudioMediaDescription();
                audioMediaDescription.setStreamIndex(streamIndex++);
                AudioCap.constructAudioMediaDescription(audioCap, audioMediaDescription);

                mediaDescriptions.add(audioMediaDescription);
            }
        }
    }

    protected void ProcessKeyFrameRequested(String participantid, Vector<MediaDescription> mediaDescriptions) {
        TerminalService terminalService = usedVmtServiceMap.get(participantid);
        if (null == terminalService) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ProcessKeyFrameRequested, not found terminal! participantid: " + participantid);
            System.out.println("ProcessKeyFrameRequested, not found terminal!");
            return;
        }

        TerminalMediaResource terminalMediaResource = terminalMediaSourceService.getTerminalMediaResource(participantid);
        if (terminalMediaResource == null) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ProcessKeyFrameRequested, terminalMediaResource is null *******!");
            System.out.println("ProcessKeyFrameRequested, terminalMediaResource is null *******!");
            return;
        }

        List<DetailMediaResouce> forwardResources = terminalService.getForwardChannel();
        if (forwardResources == null || forwardResources.isEmpty()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ProcessKeyFrameRequested, get forwardResources is null or empty *******!");
            System.out.println("ProcessKeyFrameRequested, get forwardResources is null or empty *******!");
            return;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "forwardResources size : " + forwardResources.size());
        System.out.println("forwardResources size : " + forwardResources.size());
        List<String> resourceIds = new ArrayList<>();

        for (DetailMediaResouce detailMediaResouce : forwardResources) {
            if (!"video".equals(detailMediaResouce.getType())) {
                continue;
            }

            for (MediaDescription mediaDescription : mediaDescriptions) {
                if (!baseSysConfig.isSendRecvPort()){
                    if (mediaDescription.getStreamIndex() != detailMediaResouce.getStreamIndex()) {
                        continue;
                    }
                }

                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "forwardResource type is video resourceId is : " + detailMediaResouce.getId() + ", mediaDescriptions : " + mediaDescription.toString() + ", detailMediaResouce : " + detailMediaResouce.toString());
                System.out.println("forwardResource type is video resourceId is : " + detailMediaResouce.getId() + ", mediaDescriptions : " + mediaDescriptions.toString() + ", detailMediaResouce : " + detailMediaResouce.toString());
                String resourceId = detailMediaResouce.getId();
                resourceIds.add(resourceId);
                break;
            }
        }

        terminalService.RequestKeyframe(resourceIds);
    }

    public  void processRegisterationStateChanged(String particpantId, boolean isRegistered, ServerErrorEnum err){
        TerminalService vmt = findVmt(particpantId);
        if(null != vmt){
            if(isRegistered){
                useGkStatus.incrementAndGet();
                useGkVmtServiceMap.put(particpantId,vmt);
            }else{
                if(useGkVmtServiceMap.containsKey(particpantId)){
                    useGkStatus.decrementAndGet();
                    useGkVmtServiceMap.remove(particpantId);
                }

            }
        }
    }

    public TerminalService getUseGkFreeVmt() {
        synchronized (useGkVmtServiceMap) {
            if (useGkVmtServiceMap.isEmpty()) {
                return null;
            }

            for (Map.Entry<String, TerminalService> useGkTerminalServiceEntry : useGkVmtServiceMap.entrySet()) {
                String particpantId = useGkTerminalServiceEntry.getKey();
                if(freeVmtServiceMap.containsKey(particpantId)){
                    TerminalService terminalService = freeVmtServiceMap.get(particpantId);
                    freeVmtServiceMap.remove(particpantId);
                    usedVmtServiceMap.put(particpantId, terminalService);
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "getUseGkFreeVmt, vmt :" + particpantId);
                    System.out.println("getUseGkFreeVmt, vmt :" + particpantId);
                    return terminalService;
                }
            }

            return null;
        }

    }

    public Map<String, TerminalService> getFreeVmtServiceMap() {
        return freeVmtServiceMap;
    }

    public void setFreeVmtServiceMap(Map<String, TerminalService> freeVmtServiceMap) {
        this.freeVmtServiceMap = freeVmtServiceMap;
    }

    public Map<String, TerminalService> getUsedVmtServiceMap() {
        return usedVmtServiceMap;
    }

    public void setUsedVmtServiceMap(Map<String, TerminalService> usedVmtServiceMap) {
        this.usedVmtServiceMap = usedVmtServiceMap;
    }

    public Map<String, TerminalService> getUseGkVmtServiceMap() {
        return useGkVmtServiceMap;
    }

    public void setUseGkVmtServiceMap(Map<String, TerminalService> useGkVmtServiceMap) {
        this.useGkVmtServiceMap = useGkVmtServiceMap;
    }

    public AtomicInteger getUseGkStatus() {
        return useGkStatus;
    }

    public void setUseGkStatus(AtomicInteger useGkStatus) {
        this.useGkStatus = useGkStatus;
    }

    protected IConferenceManager conferenceManager;
    protected Map<String, TerminalService> freeVmtServiceMap;
    protected Map<String, TerminalService> usedVmtServiceMap;
    protected Map<String, TerminalService> useGkVmtServiceMap = new ConcurrentHashMap<>();
    protected ConfInterfaceService confInterfaceService;
    protected TerminalMediaSourceService terminalMediaSourceService;
    private static ConfInterfacePublishService confInterfacePublishService;
    private boolean supportAliasCall;
    private AtomicInteger useGkStatus = new AtomicInteger(0);

    @Autowired
    private BaseSysConfig baseSysConfig;
}
