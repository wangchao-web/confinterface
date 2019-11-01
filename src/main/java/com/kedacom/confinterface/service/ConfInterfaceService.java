package com.kedacom.confinterface.service;

import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dao.*;
import com.kedacom.confinterface.dto.*;

import com.kedacom.confinterface.inner.*;
import com.kedacom.confinterface.restclient.McuRestClientService;
import com.kedacom.confinterface.restclient.mcu.*;
import com.kedacom.confinterface.syssetting.BaseSysConfig;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class ConfInterfaceService {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
    private final int maxVmtNum = 17;

    public List<String> getVmts() {
        return terminalMediaSourceService.getVmtList();
    }

    public List<String> addVmt(String e164) {
        return terminalMediaSourceService.addVmt(e164);
    }

    public List<String> delVmt(String e164) {
        return terminalMediaSourceService.delVmt(e164);
    }

    public Map<String, String> getGroups() {
        return terminalMediaSourceService.getGroups();
    }

    public List<Terminal> getGroupMtMembers(String groupId) {
        return terminalMediaSourceService.getGroupMtMembers(groupId);
    }

    public List<Terminal> delGroupMtMember(String groupId, Terminal mtTerminal) {
        return terminalMediaSourceService.delGroupMtMember(groupId, mtTerminal);
    }

    public List<Terminal> getGroupVmtMembers(String groupId) {
        return terminalMediaSourceService.getGroupVmtMembers(groupId);
    }

    public InspectionSrcParam getGroupInspectionParam(String e164) {
        return terminalMediaSourceService.getGroupInspectionParam(e164);
    }

    public InspectionSrcParam addGroupInspectionParam(String e164, InspectionSrcParam inspectionParam) {
        return terminalMediaSourceService.addGroupInspectionParam(e164, inspectionParam);
    }

    public InspectionSrcParam delGroupInspectionParam(String e164) {
        return terminalMediaSourceService.delGroupInspectionParam(e164);
    }

    public List<TerminalMediaResource> getTerminalMediaResources(String groupId) {
        return terminalMediaSourceService.getTerminalMediaResources(groupId);
    }

    public TerminalMediaResource getTerminalMediaResource(String mtE164) {
        return terminalMediaSourceService.getTerminalMediaResource(mtE164);
    }

    public BroadcastSrcMediaInfo getBroadcastSrc(String groupId) {
        return terminalMediaSourceService.getBroadcastSrcInfo(groupId);
    }

    public String getGroupId(String confId) {
        return confGroupMap.get(confId);
    }

    public void addGroupConfInfo(GroupConfInfo groupConfInfo) {
        confGroupMap.put(groupConfInfo.getConfId(), groupConfInfo.getGroupId());
        groupConfInfoMap.put(groupConfInfo.getGroupId(), groupConfInfo);
    }

    public void delGroupConfInfo(GroupConfInfo groupConfInfo) {
        confGroupMap.remove(groupConfInfo.getConfId());
        groupConfInfoMap.remove(groupConfInfo.getGroupId());
    }

    public GroupConfInfo getGroupConfInfo(String groupId) {
        return groupConfInfoMap.get(groupId);
    }

    public Map<String, P2PCallGroup> getP2pCallGroupMap() {
        return p2pCallGroupMap;
    }

    public void setP2pCallGroupMap(Map<String, P2PCallGroup> p2pCallGroupMap) {
        this.p2pCallGroupMap = p2pCallGroupMap;
    }

    @Async("confTaskExecutor")
    public void joinConference(JoinConferenceRequest joinConferenceRequest) {
        GroupConfInfo groupConfInfo = groupConfInfoMap.get(joinConferenceRequest.getGroupId());
        List<Terminal> joinConfMts = joinConferenceRequest.getMts();
        int mtNum = joinConfMts.size();
        String groupId = joinConferenceRequest.getGroupId();
        boolean bSetBroadcast = false;
        boolean endConf = false;
        int joinConfVmtNum = 0;
        String confId = null;
        boolean confinterface = joinConferenceRequest.isConfinterface();

        if (null == groupConfInfo) {
            groupConfInfo = new GroupConfInfo(joinConferenceRequest.getGroupId(), null);
            confId = mcuRestClientService.createConference();
            if (null == confId) {
                LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50000 : create conference failed!");
                joinConferenceRequest.makeErrorResponseMsg(ConfInterfaceResult.CREATE_CONFERENCE.getCode(), HttpStatus.OK, ConfInterfaceResult.CREATE_CONFERENCE.getMessage());
                return;
            }

            groupConfInfo.setConfinterface(confinterface);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "create conference OK, confId : " + confId);
            System.out.println("create conference OK, confId : " + confId);
            groupConfInfo.setConfId(confId);
            addGroupConfInfo(groupConfInfo);

            endConf = true;
            if(!groupConfInfo.isConfinterface()) {
                System.out.println("groupConfInfo.isConfinterface() : " +groupConfInfo.isConfinterface());
                bSetBroadcast = true;
                //endConf = true;

                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "no groupConfInfo, mtNum : " + mtNum);
                System.out.println("no groupConfInfo, mtNum : " + mtNum);
                joinConfVmtNum = mtNum + 1;
                joinConfVmtNum = joinConfVmtNum > maxVmtNum ? maxVmtNum : joinConfVmtNum;
            }
        } else {
            confId = groupConfInfo.getConfId();
            int mtNumTotal = groupConfInfo.getMtMemberNum() + mtNum;
            if(!groupConfInfo.isConfinterface()) {
                int vmtNum = groupConfInfo.getVmtMemberNum();
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "joinConference, vmtNum:" + vmtNum + ", mtNumTotal:" + mtNumTotal);
                System.out.println("joinConference, vmtNum:" + vmtNum + ", mtNumTotal:" + mtNumTotal);
                if (vmtNum < mtNumTotal + 1) {
                    if (vmtNum < maxVmtNum) {
                        joinConfVmtNum = (maxVmtNum - vmtNum) > mtNum ? mtNum : (maxVmtNum - vmtNum);
                    }
                }
            }
        }

        List<Terminal> joinConfVmts = new ArrayList<>();
        if(!groupConfInfo.isConfinterface()) {
            if (joinConfVmtNum > 0) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "joinConfVmtNum : " + joinConfVmtNum + ", bSetBroadcast :" + bSetBroadcast);
                System.out.println("joinConfVmtNum : " + joinConfVmtNum + ", bSetBroadcast :" + bSetBroadcast);
                List<TerminalService> terminalServices = terminalManageService.getFreeVmts(joinConfVmtNum);
                if (null == terminalServices) {
                    //todo:遍历目前所有的group，将空闲的vmt进行退会
                    if (endConf) {
                        mcuRestClientService.endConference(confId, true);
                        delGroupConfInfo(groupConfInfo);
                    }
                    LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50004 : reach max interface capacity!!");
                    joinConferenceRequest.makeErrorResponseMsg(ConfInterfaceResult.NO_FREE_VMT.getCode(), HttpStatus.OK, ConfInterfaceResult.NO_FREE_VMT.getMessage());
                    return;
                }

                for (TerminalService terminalService : terminalServices) {
                    terminalService.setGroupId(groupId);
                    groupConfInfo.addMember(terminalService);

                    Terminal terminal = new Terminal(terminalService.getE164());
                    joinConfVmts.add(terminal);
                }

                //设置广播源VMT
                if (bSetBroadcast) {
                    groupConfInfo.setBroadcastVmtService(null);
                }

                List<JoinConferenceRspMtInfo> vmtTerminals = mcuRestClientService.joinConference(confId, joinConfVmts);
                if (null == vmtTerminals) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "vmt join conference failed!............");
                    System.out.println("vmt join conference failed!............");
                    if (joinConfVmtNum > 0) {
                        groupConfInfo.delVmtMembers(joinConfVmts);
                        joinConfVmts.clear();
                    }

                    if (endConf) {
                        mcuRestClientService.endConference(confId, true);
                        delGroupConfInfo(groupConfInfo);
                    }

                    LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50001 : add terminal into conference failed!");
                    joinConferenceRequest.makeErrorResponseMsg(ConfInterfaceResult.ADD_TERMINAL_INTO_CONFERENCE.getCode(), HttpStatus.OK, ConfInterfaceResult.ADD_TERMINAL_INTO_CONFERENCE.getMessage());
                    return;
                }
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "vmt join conference OK ********************");
                System.out.println("vmt join conference OK ********************");
            }
        }

        for (Terminal terminal : joinConfMts) {
            TerminalService terminalService = terminalManageService.createTerminal(terminal.getMtE164(), false);
            groupConfInfo.addMember(terminalService);
        }

        List<JoinConferenceRspMtInfo> terminals = mcuRestClientService.joinConference(confId, joinConfMts);
        if (null != terminals) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "mt join conference OK, joinConfVmtNum:" + joinConfVmtNum);
            System.out.println("mt join conference OK, joinConfVmtNum:" + joinConfVmtNum);
            if (joinConfVmtNum > 0) {
                //说明本次有新的vmt被选定入会,需要写入数据库
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "start addGroupVmtMembers.............");
                System.out.println("start addGroupVmtMembers.............");
                terminalMediaSourceService.addGroupVmtMembers(groupId, joinConfVmts);
            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Join conference OK, start addGroupMtMembers and addGroup.............");
            System.out.println("Join conference OK, start addGroupMtMembers and addGroup.............");
            terminalMediaSourceService.addGroupMtMembers(groupId, joinConfMts);
            terminalMediaSourceService.setGroup(groupId, confId);
            joinConferenceRequest.makeSuccessResponseMsg();

            //与终端无关的订阅信息在此全部订阅掉
            mcuRestClientService.subscribeConfInfo(confId);
            mcuRestClientService.subscribeInspection(confId);
            mcuRestClientService.subscribeSpeaker(confId);
            mcuRestClientService.subscribeDual(confId);

            return;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "mt join conference failed!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("mt join conference failed!!!!!!!!!!!!!!!!!!!!!!!!");
        for (Terminal terminal : joinConfMts) {
            groupConfInfo.delMtMember(terminal.getMtE164());
        }

        if (joinConfVmtNum > 0) {
            //如果入会失败，需要将vmt变为空闲状态
            groupConfInfo.delVmtMembers(joinConfVmts);
            terminalManageService.freeVmts(joinConfVmts);
        }

        //失败,则需要解散会议
        if (endConf) {
            mcuRestClientService.endConference(groupConfInfo.getConfId(), true);
            delGroupConfInfo(groupConfInfo);
        }

        LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50001 : add terminal into conference failed!");
        joinConferenceRequest.makeErrorResponseMsg(ConfInterfaceResult.ADD_TERMINAL_INTO_CONFERENCE.getCode(), HttpStatus.OK, ConfInterfaceResult.ADD_TERMINAL_INTO_CONFERENCE.getMessage());
    }

    @Async("confTaskExecutor")
    public void leftConference(LeftConferenceRequest leftConferenceRequest) {
        String groupId = leftConferenceRequest.getGroupId();
        GroupConfInfo groupConfInfo = groupConfInfoMap.get(groupId);
        if (null == groupConfInfo) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "leftConference, not found groupId : " + leftConferenceRequest.getGroupId());
            System.out.println("leftConference, not found groupId : " + leftConferenceRequest.getGroupId());
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            leftConferenceRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }

        List<TerminalId> terminalIds = new ArrayList<>();
        LeftConferenceParam leftConferenceParam = leftConferenceRequest.getLeftConferenceParam();
        List<Terminal> terminals = leftConferenceParam.getMts();
        Map<String, TerminalService> terminalServiceMap = groupConfInfo.getMtMembers();
        for (Terminal terminal : terminals) {
            TerminalService terminalService = terminalServiceMap.get(terminal.getMtE164());
            if (null == terminalService)
                continue;

            TerminalId terminalId = new TerminalId(terminalService.getMtId());
            terminalIds.add(terminalId);
        }

        boolean cancelGroup = leftConferenceParam.isCancelGroup();
        McuStatus mcuStatus = mcuRestClientService.leftConference(groupConfInfo.getConfId(), terminalIds, cancelGroup);
        if (mcuStatus.getValue() == 0) {
            leftConferenceRequest.makeSuccessResponseMsg();
            terminalMediaSourceService.delGroupMtMembers(groupId, terminals);
            for (Terminal terminal : terminals) {
                terminalMediaSourceService.delGroupInspectionParam(terminal.getMtE164());
                groupConfInfo.delMtMember(terminal.getMtE164());
            }

            //以下内容放到收到会议的订阅消息时处理
            /*
            if (cancelGroup) {
                System.out.println("leftConference, cancelGroup, groupId:"+groupId);
                delGroupConfInfo(groupConfInfo);

                groupConfInfo.cancelGroup();
                terminalMediaSourceService.delGroup(groupConfInfo.getGroupId());
                terminalMediaSourceService.delGroupMtMembers(groupId, null);
                terminalMediaSourceService.delGroupVmtMembers(groupId, null);
                terminalMediaSourceService.delBroadcastSrcInfo(groupId);
            }
            */
        } else {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "left conference failed, confId:" + groupConfInfo.getConfId() + ", groupId:" + groupId);
            System.out.println("left conference failed, confId:" + groupConfInfo.getConfId() + ", groupId:" + groupId);
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50003 : del terminal from conference failed!");
            leftConferenceRequest.makeErrorResponseMsg(ConfInterfaceResult.LEFT_CONFERENCE.getCode(), HttpStatus.OK, mcuStatus.getDescription());
        }
    }

    @Async("confTaskExecutor")
    public void setBroadcastSrc(BroadCastRequest broadCastRequest) {
        String groupId = broadCastRequest.getGroupId();
        GroupConfInfo groupConfInfo = groupConfInfoMap.get(groupId);
        if (null == groupConfInfo) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            broadCastRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }

        boolean isTerminal = broadCastRequest.getBroadCastParam().isTerminalType();
        String broadcastE164 = broadCastRequest.getBroadCastParam().getMtE164();

        if(groupConfInfo.isConfinterface()){
            if(!isTerminal || broadcastE164.equals("")){
                LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "500012 : confinterface setBroadcastSrc invalid param!");
                broadCastRequest.makeErrorResponseMsg(ConfInterfaceResult.INVALID_PARAM.getCode(), HttpStatus.OK,ConfInterfaceResult.INVALID_PARAM.getMessage());
                return;
            }
        }

        TerminalService broadcastVmtService = groupConfInfo.getBroadcastVmtService();

        if (groupConfInfo.getBroadcastType() != 0) {
            //如果广播类型不为0, 说明之前已经设置过广播
            boolean theSameOne = false;
            if (isTerminal && groupConfInfo.isTerminalType() && broadcastE164.equals(groupConfInfo.getBroadcastMtE164())) {
                theSameOne = true;
            } else if (!isTerminal && !groupConfInfo.isTerminalType()) {
                theSameOne = true;
                broadcastE164 = broadcastVmtService.getE164();
            }

            if (theSameOne) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "setBroadcastSrc, terminal:" + broadcastE164 + ",type:" + groupConfInfo.getBroadcastType() + ", group exist the same broadcast src!");
                System.out.println("setBroadcastSrc, terminal:" + broadcastE164 + ",type:" + groupConfInfo.getBroadcastType() + ", group exist the same broadcast src!");
                broadCastRequest.setForwardResources(TerminalMediaResource.convertToMediaResource(broadcastVmtService.getForwardChannel(), "all"));
                broadCastRequest.setReverseResources(TerminalMediaResource.convertToMediaResource(broadcastVmtService.getReverseChannel(), "all"));
                broadCastRequest.makeSuccessResponseMsg();
                return;
            }
        }

        String broadcastMtId;
        if (!isTerminal) {
            broadcastMtId = broadcastVmtService.getMtId();
        } else {
            TerminalService mtTerminalService = groupConfInfo.getMtMember(broadcastE164);
            broadcastMtId = mtTerminalService.getMtId();
        }

        String channel = getSpeakerChannel(groupConfInfo.getConfId());
        broadCastRequest.addWaitMsg(channel);
        groupConfInfo.addWaitDealTask(channel, broadCastRequest);

        McuStatus mcuStatus = mcuRestClientService.setSpeaker(groupConfInfo.getConfId(), broadcastMtId);
        if (mcuStatus.getValue() == 0) {
            groupConfInfo.setBroadcastType(broadCastRequest.getBroadCastParam().getType());
            groupConfInfo.setBroadcastMtE164(broadCastRequest.getBroadCastParam().getMtE164());
            return;
        }

        broadCastRequest.removeMsg(channel);
        groupConfInfo.delWaitDealTask(channel);
        LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50009 : set speaker failed!");
        broadCastRequest.makeErrorResponseMsg(ConfInterfaceResult.SET_SPEAKER.getCode(), HttpStatus.OK, mcuStatus.getDescription());
    }

    @Async("confTaskExecutor")
    public void cancelBroadcast(CancelBroadCastRequest cancelBroadCastRequest) {
        String groupId = cancelBroadCastRequest.getGroupId();
        GroupConfInfo groupConfInfo = groupConfInfoMap.get(groupId);
        if (null == groupConfInfo) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            cancelBroadCastRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }

        McuStatus mcuStatus = mcuRestClientService.cancelSpeaker(groupConfInfo.getConfId());
        if (mcuStatus.getValue() == 0) {
            groupConfInfo.setBroadcastType(BroadcastTypeEnum.UNKNOWN.getCode());
            groupConfInfo.setBroadcastMtE164(null);
            //更新数据库中的广播源信息
            BroadcastSrcMediaInfo broadcastSrcMediaInfo = terminalMediaSourceService.getBroadcastSrcInfo(groupId);
            broadcastSrcMediaInfo.setType(BroadcastTypeEnum.UNKNOWN.getCode());
            broadcastSrcMediaInfo.setMtE164(null);
            terminalMediaSourceService.setBroadcastSrcInfo(groupId, broadcastSrcMediaInfo);

            cancelBroadCastRequest.makeSuccessResponseMsg();
        } else {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50010 : cancel speaker failed!");
            cancelBroadCastRequest.makeErrorResponseMsg(ConfInterfaceResult.CANCEL_SPEAKER.getCode(), HttpStatus.OK, mcuStatus.getDescription());
        }
    }

    @Async("confTaskExecutor")
    public void joinDiscussionGroup(JoinDiscussionGroupRequest joinDiscussionGroupRequest) {
        String groupId = joinDiscussionGroupRequest.getGroupId();
        GroupConfInfo groupConfInfo = groupConfInfoMap.get(groupId);
        if (null == groupConfInfo) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            joinDiscussionGroupRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }

        boolean bOk = false;
        List<Terminal> joinDiscussionMts = joinDiscussionGroupRequest.getMts();
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "joinDiscussionGroup, join discussion mts :" + joinDiscussionMts.size());
        System.out.println("joinDiscussionGroup, join discussion mts :" + joinDiscussionMts.size());
        for (Terminal terminal : joinDiscussionMts) {
            bOk |= joinDiscusssion(groupConfInfo, terminal, joinDiscussionGroupRequest);
        }

        if (!bOk) {
            //如果失败，则表明加入讨论组均失败,直接回复失败
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50005 : inspection terminal failed!");
            joinDiscussionGroupRequest.makeErrorResponseMsg(ConfInterfaceResult.INSPECTION.getCode(), HttpStatus.OK, ConfInterfaceResult.INSPECTION.getMessage());
        }
    }

    @Async("confTaskExecutor")
    public void leftDiscussionGroup(LeftDiscussionGroupRequest leftDiscussionGroupRequest) {
        String groupId = leftDiscussionGroupRequest.getGroupId();
        GroupConfInfo groupConfInfo = groupConfInfoMap.get(groupId);
        if (null == groupConfInfo) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            leftDiscussionGroupRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }

        String confId = groupConfInfo.getConfId();
        List<LeftDiscussionGroupMt> leftDiscussionMts = leftDiscussionGroupRequest.getMts();
        if (null == leftDiscussionMts || leftDiscussionMts.isEmpty()) {
            leftDiscussionGroupRequest.makeSuccessResponseMsg();
            return;
        }

        boolean bSendResponse = true;
        String videochannel;
        String audioChannel;
        Iterator<LeftDiscussionGroupMt> leftDiscussinMtIterator = leftDiscussionMts.iterator();
        while (leftDiscussinMtIterator.hasNext()) {
            LeftDiscussionGroupMt leftDiscussionGroupMt = leftDiscussinMtIterator.next();

            TerminalService mtService = groupConfInfo.getMtMember(leftDiscussionGroupMt.getMtE164());
            if (mtService.getInspectionParam() == null) {
                leftDiscussinMtIterator.remove();
                continue;
            }

            bSendResponse = false;
            videochannel = getInspectionChannel(confId, mtService.getMtId(), InspectionModeEnum.VIDEO.getCode());
            leftDiscussionGroupRequest.addWaitMsg(videochannel);
            groupConfInfo.addWaitDealTask(videochannel, leftDiscussionGroupRequest);

            audioChannel = getInspectionChannel(confId, mtService.getMtId(), InspectionModeEnum.AUDIO.getCode());
            leftDiscussionGroupRequest.addWaitMsg(audioChannel);
            groupConfInfo.addWaitDealTask(audioChannel, leftDiscussionGroupRequest);

            McuStatus mcuStatus = mcuRestClientService.cancelInspection(confId, InspectionModeEnum.ALL.getName(), mtService.getMtId());
            if (mcuStatus.getValue() == 0) {
                //只要会议终端成功取消选看，则认为退出讨论成功，从列表中移除
                TerminalService vmtService = groupConfInfo.getSrcInspectionTerminal(mtService);
                if (leftDiscussionGroupMt.isStopInspection()) {
                    if (null != vmtService.getInspectionParam() && vmtService.getInspectionParam().getMtE164().equals(mtService.getE164())) {
                        videochannel = getInspectionChannel(confId, vmtService.getMtId(), InspectionModeEnum.VIDEO.getCode());
                        leftDiscussionGroupRequest.addWaitMsg(videochannel);
                        groupConfInfo.addWaitDealTask(videochannel, leftDiscussionGroupRequest);

                        audioChannel = getInspectionChannel(confId, vmtService.getMtId(), InspectionModeEnum.AUDIO.getCode());
                        leftDiscussionGroupRequest.addWaitMsg(audioChannel);
                        groupConfInfo.addWaitDealTask(audioChannel, leftDiscussionGroupRequest);

                        mcuStatus = mcuRestClientService.cancelInspection(confId, InspectionModeEnum.ALL.getName(), vmtService.getMtId());
                    }
                }

                leftDiscussinMtIterator.remove();
            }

            if (mcuStatus.getValue() > 0) {
                //其中一个终端退出失败,继续处理其他终端
                groupConfInfo.delWaitDealTask(videochannel);
                groupConfInfo.delWaitDealTask(audioChannel);
                leftDiscussionGroupRequest.removeMsg(videochannel);
                leftDiscussionGroupRequest.removeMsg(audioChannel);
            }
        }

        if (bSendResponse) {
            leftDiscussionGroupRequest.makeSuccessResponseMsg();
        }
    }

    @Async("confTaskExecutor")
    public void startInspection(InspectionRequest inspectionRequest) {
        String groupId = inspectionRequest.getGroupId();
        GroupConfInfo groupConfInfo = groupConfInfoMap.get(groupId);
        if (null == groupConfInfo) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            inspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }

        boolean bInspection = true;
        InspectionParam inspectionParam = inspectionRequest.getInspectionParam();
        int mode = InspectionModeEnum.resolve(inspectionParam.getMode()).getCode();

        InspectionSrcParam inspectionSrcParam = new InspectionSrcParam();
        String dstInspectionE164 = inspectionParam.getDstMtE164();
        String srcInspectionE164 = inspectionParam.getSrcMtE164();

        if(groupConfInfo.isConfinterface()){
            if("".equals(srcInspectionE164) || "".equals(dstInspectionE164)){
                System.out.println(" confinterface startInspection, dstInspectionE164 || srcInspectionE164 are empty! not permitted!");
                LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, " 50012 : confinterface cancelInspectionRequest invalid param!");
                inspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.INVALID_PARAM.getCode(), HttpStatus.OK, ConfInterfaceResult.INVALID_PARAM.getMessage());
                return;
            }
        }

        if (dstInspectionE164.isEmpty() && srcInspectionE164.isEmpty()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, dstInspectionE164 & srcInspectionE164 are empty! not permitted!");
            System.out.println("startInspection, dstInspectionE164 & srcInspectionE164 are empty! not permitted!");
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50012 : invalid param!");
            inspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.INVALID_PARAM.getCode(), HttpStatus.OK, ConfInterfaceResult.INVALID_PARAM.getMessage());
            return;
        }

        TerminalService srcService = null;
        TerminalService dstService = null;
        boolean bResume = false;
        String nowMode = InspectionModeEnum.ALL.getName();
        if (!srcInspectionE164.isEmpty()) {
            //如果选看源不空,则一定是会议终端,获取会议终端服务
            srcService = groupConfInfo.getMtMember(srcInspectionE164);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, srcInspectionE164 is not empty, e164:" + srcInspectionE164 + ", mtId:" + srcService.getMtId() + ", dstInspectionE164:" + dstInspectionE164);
            System.out.println("startInspection, srcInspectionE164 is not empty, e164:" + srcInspectionE164 + ", mtId:" + srcService.getMtId() + ", dstInspectionE164:" + dstInspectionE164);

            if (dstInspectionE164.isEmpty()) {
                TerminalService dstVmtService = groupConfInfo.getDstInspectionVmtTerminal(srcService);
                if (null != dstVmtService) {
                    InspectionSrcParam nowInspectionParam = dstVmtService.getInspectionParam();
                    nowMode = nowInspectionParam.getMode();
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, vmt(" + dstVmtService.getE164() + ") has inspect terminal(" + srcInspectionE164 + "), nowMode:" + nowMode + ", inspectMode:" + inspectionParam.getMode());
                    System.out.println("startInspection, vmt(" + dstVmtService.getE164() + ") has inspect terminal(" + srcInspectionE164 + "), nowMode:" + nowMode + ", inspectMode:" + inspectionParam.getMode());
                    //说明已经有虚拟终端选看了该会议终端,判断选看模式
                    if (nowMode.equals(inspectionParam.getMode()) || nowMode.equals(InspectionModeEnum.ALL.getName())) {
                        //选看模式一样，或者不一样时但当前选看已经是all，直接返回资源信息
                        List<DetailMediaResouce> reverseDetailResource = dstVmtService.getReverseChannel();
                        inspectionRequest.makeSuccessResponseMsg(TerminalMediaResource.convertToMediaResource(reverseDetailResource, inspectionParam.getMode()));
                        return;
                    }

                    dstService = dstVmtService;
                    nowInspectionParam.setMode(InspectionModeEnum.ALL.getName());
                    dstService.setInspectStatus(mode, InspectionStatusEnum.UNKNOWN.getCode());
                    bResume = true;
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, init status of mode(" + inspectionParam.getMode() + "," + mode + ") to unknown! src:" + srcInspectionE164);
                    System.out.println("startInspection, init status of mode(" + inspectionParam.getMode() + "," + mode + ") to unknown! src:" + srcInspectionE164);
                } else {
                    //从使用的VMT中选择一个没有选看任何终端的服务
                    dstService = groupConfInfo.getNoInspectTerminalServiceFromUsedVmtMember();
                    if (null != dstService) {
                        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, choose vmt from usedVmt, e164:" + dstService.getE164());
                        System.out.println("startInspection, choose vmt from usedVmt, e164:" + dstService.getE164());
                    }
                }
            }
        }

        if (null == dstService && !dstInspectionE164.isEmpty()) {
            //目的是会议终端或者指定虚拟终端进行选看(指定虚拟终端的情况只发生在会议终端下线后再上线的选看恢复!!!!)
            dstService = groupConfInfo.getMember(dstInspectionE164);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, dstInspectionE164 is not empty, e164:" + dstInspectionE164 + ", mtId:" + dstService.getMtId());
            System.out.println("startInspection, dstInspectionE164 is not empty, e164:" + dstInspectionE164 + ", mtId:" + dstService.getMtId());

            //判断是否存在选看
            if (dstService.isInspection()) {
                InspectionSrcParam oldInspectionSrcParam = dstService.getInspectionParam();
                String inspectE164 = oldInspectionSrcParam.getMtE164();
                nowMode = oldInspectionSrcParam.getMode();
                TerminalService srcInspectionService = groupConfInfo.getMember(inspectE164);

                if (null == srcService && srcInspectionService.isVmt()) {
                    //已经选看了虚拟终端，判断模式选看模式
                    if (nowMode.equals(inspectionParam.getMode()) || nowMode.equals(InspectionModeEnum.ALL.getName())) {
                        //选看模式一样，或者不一样时但当前选看已经是all，直接返回资源信息
                        List<DetailMediaResouce> forwardDetailResource = srcInspectionService.getForwardChannel();
                        inspectionRequest.makeSuccessResponseMsg(TerminalMediaResource.convertToMediaResource(forwardDetailResource, inspectionParam.getMode()));
                        return;
                    }

                    bResume = true;
                    oldInspectionSrcParam.setMode(InspectionModeEnum.ALL.getName());
                    dstService.setInspectStatus(mode, InspectionStatusEnum.UNKNOWN.getCode());
                    srcService = srcInspectionService;
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, init status of mode(" + inspectionParam.getMode() + "," + mode + ") to unknown! vmt src:" + srcInspectionE164 + ", mt dst:" + dstInspectionE164);
                    System.out.println("startInspection, init status of mode(" + inspectionParam.getMode() + "," + mode + ") to unknown! vmt src:" + srcInspectionE164 + ", mt dst:" + dstInspectionE164);
                } else if (null != srcService && srcInspectionE164.equals(inspectE164)) {
                    //选看了同一个会议终端，判断选看模式
                    if (nowMode.equals(inspectionParam.getMode()) || nowMode.equals(InspectionModeEnum.ALL.getName())) {
                        inspectionRequest.makeSuccessResponseMsg(new ArrayList<>());
                        return;
                    }

                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, init status of mode(" + inspectionParam.getMode() + "," + mode + ") to unknown! mt src:" + srcInspectionE164 + ", mt dst:" + dstInspectionE164);
                    System.out.println("startInspection, init status of mode(" + inspectionParam.getMode() + "," + mode + ") to unknown! mt src:" + srcInspectionE164 + ", mt dst:" + dstInspectionE164);
                    bResume = true;
                    oldInspectionSrcParam.setMode(InspectionModeEnum.ALL.getName());
                    dstService.setInspectStatus(mode, InspectionStatusEnum.UNKNOWN.getCode());
                } else {
                    //如果已经存在选看，此时又请求选看别的终端，直接返回失败
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, has inspected other terminal:" + inspectE164);
                    System.out.println("startInspection, has inspected other terminal:" + inspectE164);
                    LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50013 : inspect other terminal!");
                    inspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.INSPECTION_OTHER_TERMINAL.getCode(), HttpStatus.OK, ConfInterfaceResult.INSPECTION_OTHER_TERMINAL.getMessage());
                    return;
                }
            } else if (null == srcService) {
                //从使用的VMT中选择一个没有被任何会议终端选看的服务
                srcService = groupConfInfo.getNotBeInspectedTerminalServiceFromUsedVmtMember();
                if (null != srcService) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, choose vmt from usedVmt for inspction src, e164:" + srcService.getE164());
                    System.out.println("startInspection, choose vmt from usedVmt for inspction src, e164:" + srcService.getE164());
                }
            }
        }

        if (null != srcService && !srcService.isOnline()
                || null != dstService && !dstService.isOnline()) {
            //选看的源不在线
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, src or dst is offline!");
            System.out.println("startInspection, src or dst is offline!");
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50011 : terminal is offline!");
            inspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.OFFLINE.getCode(), HttpStatus.OK, ConfInterfaceResult.OFFLINE.getMessage());
            return;
        }

        if (null == dstService || null == srcService) {
            int freeVmtNum = groupConfInfo.getFreeVmtMemberNum();
            TerminalService vmtService = groupConfInfo.getAndUseVmt(null);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, dst or src is empty, choose free vmt, free vmt num:" + freeVmtNum);
            System.out.println("startInspection, dst or src is empty, choose free vmt, free vmt num:" + freeVmtNum);
            if (null == vmtService) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, vmtService == null!");
                System.out.println("startInspection, vmtService == null!");
                if (freeVmtNum > 0) {
                    LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50018 : no vmt online, please wait a minute!");
                    inspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.VMT_NOT_ONLINE.getCode(), HttpStatus.OK, ConfInterfaceResult.VMT_NOT_ONLINE.getMessage());
                    return;
                }

                vmtService = chooseVmt(groupConfInfo, inspectionRequest, true);
                if (null == vmtService) {
                    return;
                }
                bInspection = false;  //等待加入会议成功之后，再进行选看
            }

            if (null == dstService) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, set dstService!!!!");
                System.out.println("startInspection, set dstService!!!!");
                dstService = vmtService;
            } else if (null == srcService) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, set srcService!!!!");
                System.out.println("startInspection, set srcService!!!!");
                srcService = vmtService;
            }
        }

        if (null == dstService.getInspectionParam()) {
            inspectionSrcParam.setMtE164(srcService.getE164());
            inspectionSrcParam.setMode(inspectionParam.getMode());
            //设置选看参数
            dstService.setInspectionParam(inspectionSrcParam);
            dstService.setInspectionStatus(InspectionStatusEnum.UNKNOWN);
            dstService.setInspectAudioStatus(InspectionStatusEnum.UNKNOWN.getCode());
            dstService.setInspectVideoStatus(InspectionStatusEnum.UNKNOWN.getCode());
        }

        if (!bInspection) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspection, no need inspection!");
            System.out.println("startInspection, no need inspection!");
            return;
        }

        boolean bOk = inspectionMt(groupConfInfo, inspectionParam.getMode(), srcService.getMtId(), dstService.getMtId(), inspectionRequest);
        if (!bOk && bResume) {
            //恢复选看模式
            dstService.getInspectionParam().setMode(nowMode);
        }
    }

    @Async("confTaskExecutor")
    public void cancelInspection(CancelInspectionRequest cancelInspectionRequest) {
        String groupId = cancelInspectionRequest.getGroupId();
        GroupConfInfo groupConfInfo = groupConfInfoMap.get(groupId);
        if (null == groupConfInfo) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            cancelInspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }

        String confId = groupConfInfo.getConfId();
        InspectionParam cancleInspectionParam = cancelInspectionRequest.getInspectionParam();
        String srcE164 = cancleInspectionParam.getSrcMtE164();
        String dstE164 = cancleInspectionParam.getDstMtE164();

        if(groupConfInfo.isConfinterface()){
            if("".equals(srcE164) || "".equals(dstE164)){
                System.out.println(" confinterface cancelInspection, dstInspectionE164 || srcInspectionE164 are empty! not permitted!");
                LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, " 50012 : cancelInspectionRequest invalid param!");
                cancelInspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.INVALID_PARAM.getCode(), HttpStatus.OK, ConfInterfaceResult.INVALID_PARAM.getMessage());
                return;
            }
        }

        if (srcE164.isEmpty() && dstE164.isEmpty()) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50012 : invalid param!");
            cancelInspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.INVALID_PARAM.getCode(), HttpStatus.OK, ConfInterfaceResult.INVALID_PARAM.getMessage());
            return;
        }

        TerminalService dstService;
        if (dstE164.isEmpty()) {
            //如果目的为空，源一定不为空
            TerminalService srcService = groupConfInfo.getMtMember(srcE164);
            dstService = groupConfInfo.getDstInspectionVmtTerminal(srcService);
            if (null == dstService) {
                //如果没有被任何虚拟终端选看，直接返回成功
                cancelInspectionRequest.makeSuccessResponseMsg();
                return;
            }
        } else {
            dstService = groupConfInfo.getMtMember(dstE164);
        }

        //校验选看模式
        InspectionSrcParam inspectionSrcParam = dstService.getInspectionParam();
        String nowMode = inspectionSrcParam.getMode();
        if (!nowMode.equals(InspectionModeEnum.ALL.getName()) && !nowMode.equals(cancleInspectionParam.getMode())) {
            //如果当前选看模式不是all，且与本次取消选看模式不同，则返回失败
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "cancelInspection, nowMode:" + nowMode + ", cancelMode:" + cancleInspectionParam.getMode() + ", not consistent!");
            System.out.println("cancelInspection, nowMode:" + nowMode + ", cancelMode:" + cancleInspectionParam.getMode() + ", not consistent!");
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50012 : invalid param!");
            cancelInspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.INVALID_PARAM.getCode(), HttpStatus.OK, ConfInterfaceResult.INVALID_PARAM.getMessage());
            return;
        }

        String dstMtId = dstService.getMtId();
        if (InspectionModeEnum.ALL.getName().equals(cancleInspectionParam.getMode()) || InspectionModeEnum.VIDEO.getName().equals(cancleInspectionParam.getMode())) {
            String channel = getInspectionChannel(confId, dstMtId, InspectionModeEnum.VIDEO.getCode());  //视频
            cancelInspectionRequest.addWaitMsg(channel);
            groupConfInfo.addWaitDealTask(channel, cancelInspectionRequest);
        }

        if (InspectionModeEnum.ALL.getName().equals(cancleInspectionParam.getMode()) || InspectionModeEnum.AUDIO.getName().equals(cancleInspectionParam.getMode())) {
            String channel = getInspectionChannel(confId, dstMtId, InspectionModeEnum.AUDIO.getCode()); //音频
            cancelInspectionRequest.addWaitMsg(channel);
            groupConfInfo.addWaitDealTask(channel, cancelInspectionRequest);
        }

        boolean bResume = false;
        if (nowMode.equals(InspectionModeEnum.ALL.getName())) {
            if (InspectionModeEnum.VIDEO.getName().equals(cancleInspectionParam.getMode())) {
                inspectionSrcParam.setMode(InspectionModeEnum.AUDIO.getName());
                bResume = true;
            } else if (InspectionModeEnum.AUDIO.getName().equals(cancleInspectionParam.getMode())) {
                inspectionSrcParam.setMode(InspectionModeEnum.VIDEO.getName());
                bResume = true;
            }
        }

        McuStatus mcuStatus = mcuRestClientService.cancelInspection(confId, cancleInspectionParam.getMode(), dstMtId);
        if (mcuStatus.getValue() == 0) {
            return;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "cancelInspection, cancel inspections failed!");
        System.out.println("cancelInspection, cancel inspections failed!");
        List<String> channels = cancelInspectionRequest.getWaitMsg();
        for (String channel : channels) {
            groupConfInfo.delWaitDealTask(channel);
        }
        cancelInspectionRequest.setWaitMsg(null);
        if (bResume) {
            inspectionSrcParam.setMode(nowMode);
        }

        LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50006 : cancel inspection terminal failed!");
        cancelInspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.CANCEL_INSPECTION.getCode(), HttpStatus.OK, mcuStatus.getDescription());
    }

    @Async("confTaskExecutor")
    public void ctrlCamera(CameraCtrlRequest cameraCtrlRequest) {
        String groupId = cameraCtrlRequest.getGroupId();
        GroupConfInfo groupConfInfo = groupConfInfoMap.get(groupId);
        if (null == groupConfInfo) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            cameraCtrlRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }

        String confId = groupConfInfo.getConfId();
        TerminalService mtService = groupConfInfo.getMtMember(cameraCtrlRequest.getMtE164());
        if (null == mtService) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50015 : terminal not exist in this conference!");
            cameraCtrlRequest.makeErrorResponseMsg(ConfInterfaceResult.TERMINAL_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.TERMINAL_NOT_EXIST.getMessage());
            return;
        }

        McuStatus mcuStatus = mcuRestClientService.ctrlCamera(confId, mtService.getMtId(), cameraCtrlRequest.getCameraCtrlParam());
        if (null == mcuStatus || mcuStatus.getValue() > 0) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50007 : control camera failed!");
            cameraCtrlRequest.makeErrorResponseMsg(ConfInterfaceResult.CONTROL_CAMERA.getCode(), HttpStatus.OK, mcuStatus.getDescription());
            return;
        }

        cameraCtrlRequest.makeSuccessResponseMsg();
    }

    @Async("confTaskExecutor")
    public void sendIFrame(SendIFrameRequest sendIFrameRequest) {
        /*String groupId = sendIFrameRequest.getGroupId();
        GroupConfInfo groupConfInfo = groupConfInfoMap.get(groupId);
        if (null == groupConfInfo) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            sendIFrameRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }

        //String confId = groupConfInfo.getConfId();
        SendIFrameParam sendIFrameParam = sendIFrameRequest.getSendIFrameParam();
        List<String> resourceIds = sendIFrameParam.getResourceIds();
        if (sendIFrameParam.getMtE164().isEmpty() || resourceIds.isEmpty()) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50012 : invalid param!");
            sendIFrameRequest.makeErrorResponseMsg(ConfInterfaceResult.INVALID_PARAM.getCode(), HttpStatus.OK, ConfInterfaceResult.INVALID_PARAM.getMessage());
            return;
        }

        TerminalService mtService = groupConfInfo.getMtMember(sendIFrameParam.getMtE164());
        if (null == mtService) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50015 : terminal not exist in this conference!");
            sendIFrameRequest.makeErrorResponseMsg(ConfInterfaceResult.TERMINAL_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.TERMINAL_NOT_EXIST.getMessage());
            return;
        }

        Termin
        alService vmtService = groupConfInfo.getDstInspectionVmtTerminal(mtService);
        if

        (null == vmtService) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50012 : invalid param!");
            sendIFrameRequest.makeErrorResponseMsg(ConfInterfaceResult.INVALID_PARAM.getCode(), HttpStatus.OK, ConfInterfaceResult.INVALID_PARAM.getMessage());
            return;
        }*/

        String groupId = sendIFrameRequest.getGroupId();
        P2PCallGroup p2PCallGroup = p2pCallGroupMap.get(groupId);
        if (null == p2PCallGroup) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            sendIFrameRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }


        SendIFrameParam sendIFrameParam = sendIFrameRequest.getSendIFrameParam();
        String account = sendIFrameParam.getMtE164();
        TerminalService vmtService = p2PCallGroup.getVmt(account);
        if (null == vmtService) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50015 : terminal not exist in this conference!");
            sendIFrameRequest.makeErrorResponseMsg(ConfInterfaceResult.TERMINAL_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.TERMINAL_NOT_EXIST.getMessage());
            return;
        }

        boolean bOK = vmtService.sendIFrame();
/*
        List<DetailMediaResouce> reverseResources = vmtService.getReverseChannel();
        Iterator<String> iterator = resourceIds.iterator();
        while (iterator.hasNext()) {
            String resourceId = iterator.next();
            for (DetailMediaResouce detailMediaResouce : reverseResources) {
                if (!resourceId.equals(detailMediaResouce.getId()))
                    continue;

                McuStatus mcuStatus = mcuRestClientService.sendIFrame(confId, detailMediaResouce.getRtp());
                if (mcuStatus.getValue() == 0) {
                    iterator.remove();
                } else {
                    System.out.println("sendIFrame failed! resourceId:"+resourceId+", dst:"+detailMediaResouce.getRtp()+", errmsg:"+mcuStatus.getDescription());
                }
                break;
            }
        }
*/
        if (bOK/*resourceIds.isEmpty()*/) {
            sendIFrameRequest.makeSuccessResponseMsg();
        } else {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50014 : send i frame!");
            sendIFrameRequest.makeErrorResponseMsg(ConfInterfaceResult.SENDIFRAME.getCode(), HttpStatus.OK, ConfInterfaceResult.SENDIFRAME.getMessage());
        }
    }

    @Async("confTaskExecutor")
    public void ctrlVolume(CtrlVolumeRequest ctrlVolumeRequest) {
        String groupId = ctrlVolumeRequest.getGroupId();
        GroupConfInfo groupConfInfo = groupConfInfoMap.get(groupId);
        if (null == groupConfInfo) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            ctrlVolumeRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }

        CtrlVolumeParam ctrlVolumeParam = ctrlVolumeRequest.getCtrlVolumeParam();
        if (ctrlVolumeRequest.getMtE164().isEmpty() || !ctrlVolumeParam.checkModeValidity()) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50012 : invalid param!");
            ctrlVolumeRequest.makeErrorResponseMsg(ConfInterfaceResult.INVALID_PARAM.getCode(), HttpStatus.OK, ConfInterfaceResult.INVALID_PARAM.getMessage());
            return;
        }

        String confId = groupConfInfo.getConfId();
        TerminalService mtService = groupConfInfo.getMtMember(ctrlVolumeRequest.getMtE164());
        if (null == mtService) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50015 : terminal not exist in this conference!");
            ctrlVolumeRequest.makeErrorResponseMsg(ConfInterfaceResult.TERMINAL_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.TERMINAL_NOT_EXIST.getMessage());
            return;
        }

        McuCtrlVolumeParam mcuCtrlVolumeParam = new McuCtrlVolumeParam();
        mcuCtrlVolumeParam.setVol_mode(ctrlVolumeParam.getMode());

        if (ctrlVolumeParam.getVolume() <= 0) {
            mcuCtrlVolumeParam.setVol_value(0);
        } else if (ctrlVolumeParam.getVolume() >= 35) {
            mcuCtrlVolumeParam.setVol_value(35);
        } else {
            mcuCtrlVolumeParam.setVol_value(ctrlVolumeParam.getVolume());
        }

        McuStatus mcuStatus = mcuRestClientService.ctrlVolume(confId, mtService.getMtId(), mcuCtrlVolumeParam);
        if (mcuStatus.getValue() == 0) {
            ctrlVolumeRequest.makeSuccessResponseMsg();
        } else {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50016 : control terminal volume failed!");
            ctrlVolumeRequest.makeErrorResponseMsg(ConfInterfaceResult.CTRL_VOLUME.getCode(), HttpStatus.OK, mcuStatus.getDescription());
        }
    }

    @Async("confTaskExecutor")
    public void silenceOrMute(CtrlSilenceOrMuteRequest ctrlSilenceOrMuteRequest) {
        String groupId = ctrlSilenceOrMuteRequest.getGroupId();
        GroupConfInfo groupConfInfo = groupConfInfoMap.get(groupId);
        if (null == groupConfInfo) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            ctrlSilenceOrMuteRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }

        String confId = groupConfInfo.getConfId();
        TerminalService mtService = groupConfInfo.getMtMember(ctrlSilenceOrMuteRequest.getMtE164());
        if (null == mtService) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50015 : terminal not exist in this conference!");
            ctrlSilenceOrMuteRequest.makeErrorResponseMsg(ConfInterfaceResult.TERMINAL_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.TERMINAL_NOT_EXIST.getMessage());
            return;
        }

        McuStatus mcuStatus = mcuRestClientService.silenceOrMute(confId, mtService.getMtId(), ctrlSilenceOrMuteRequest.isSilence(), ctrlSilenceOrMuteRequest.getSilenceOrMuteParam());
        if (mcuStatus.getValue() == 0) {
            ctrlSilenceOrMuteRequest.makeSuccessResponseMsg();
        } else {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50017 : control silence or mute failed!");
            ctrlSilenceOrMuteRequest.makeErrorResponseMsg(ConfInterfaceResult.CTRL_SILENCE_OR_MUTE.getCode(), HttpStatus.OK, mcuStatus.getDescription());
        }
    }

    @Async("confTaskExecutor")
    public void ctrlDualStream(BaseRequestMsg ctrlDualStreamRequest, String mtE164, boolean dual) {
        String groupId = ctrlDualStreamRequest.getGroupId();
        GroupConfInfo groupConfInfo = groupConfInfoMap.get(groupId);
        if (null == groupConfInfo) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            ctrlDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }

        TerminalService terminalService;
        if (mtE164.isEmpty()) {
            terminalService = groupConfInfo.getBroadcastVmtService();
        } else {
            terminalService = groupConfInfo.getMtMember(mtE164);
        }

        if (null == terminalService) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50015 : terminal not exist in this conference!");
            ctrlDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.TERMINAL_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.TERMINAL_NOT_EXIST.getMessage());
            return;
        }

        if (mtE164.isEmpty()) {
            ctrlVmtDualStream(terminalService, dual, ctrlDualStreamRequest);
        } else {
            ctrlMtDualStream(terminalService, dual, groupConfInfo, ctrlDualStreamRequest);
        }
    }

    @Async("confTaskExecutor")
    public void queryDualStream(QueryDualStreamRequest queryDualStreamRequest) {
        String groupId = queryDualStreamRequest.getGroupId();
        GroupConfInfo groupConfInfo = groupConfInfoMap.get(groupId);
        if (null == groupConfInfo) {
            queryDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }

        TerminalService terminalService = groupConfInfo.getBroadcastVmtService();
        List<DetailMediaResouce> reverseResources = terminalService.getReverseChannel();
        List<DetailMediaResouce> forwardResources = terminalService.getForwardChannel();

        int tryTimes = 2;
        boolean bFindDual = false;
        List<DetailMediaResouce> mediaResources = reverseResources;
        queryDualStreamRequest.setType(1);

        do {
            for (DetailMediaResouce detailMediaResouce : mediaResources) {
                if (detailMediaResouce.getDual() != 1) {
                    continue;
                }

                bFindDual = true;
                MediaResource mediaResource = new MediaResource();
                mediaResource.setDual(true);
                mediaResource.setId(detailMediaResouce.getId());
                mediaResource.setType(detailMediaResouce.getType());

                queryDualStreamRequest.addResource(mediaResource);
            }

            if (bFindDual) {
                queryDualStreamRequest.makeSuccessResponseMsg();
                return;
            } else {
                mediaResources = forwardResources;
                queryDualStreamRequest.setType(2);
            }
        } while (--tryTimes > 0);

        queryDualStreamRequest.setType(0);
        queryDualStreamRequest.makeSuccessResponseMsg();
    }

    @Async("confTaskExecutor")
    public void queryVmts(QueryVmtsRequest queryVmtsRequest) {
        List<TerminalService> usedVmts = terminalManageService.queryAllUsedVmts();
        if (null == usedVmts) {
            queryVmtsRequest.makeSuccessResponseMsg();
            return;
        }

        for (TerminalService usedVmt : usedVmts) {
            VmtDetailInfo vmtDetailInfo = new VmtDetailInfo();
            vmtDetailInfo.setGroupId(usedVmt.getGroupId());
            vmtDetailInfo.setVmtE164(usedVmt.getE164());
            vmtDetailInfo.setForwardResources(TerminalMediaResource.convertToMediaResource(usedVmt.getForwardChannel(), "all"));
            vmtDetailInfo.setReverseResources(TerminalMediaResource.convertToMediaResource(usedVmt.getReverseChannel(), "all"));

            queryVmtsRequest.addVmtDetailInfos(vmtDetailInfo);
        }

        queryVmtsRequest.makeSuccessResponseMsg();
        usedVmts.clear();
    }

    @Async("confTaskExecutor")
    public void p2pCall(P2PCallRequest p2PCallRequest, P2PCallParam p2PCallParam) {
        final String groupId = p2PCallRequest.getGroupId();
        String mtAccount = p2PCallParam.getAccount();

        P2PCallGroup p2PCallGroup = p2pCallGroupMap.computeIfAbsent(groupId, k -> new P2PCallGroup(groupId));
      /*  if (p2PCallParam.isDual()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "mtAccount : " + mtAccount);
            System.out.println("mtAccount : " + mtAccount);
            TerminalService vmt = p2PCallGroup.getVmt(mtAccount);
            if (null != vmt) {
                ctrlVmtDualStream(vmt, true, p2PCallRequest);
            } else {
                LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50026 : mainstream doesn't exist");
                p2PCallRequest.makeErrorResponseMsg(ConfInterfaceResult.MAINSTREAM_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.MAINSTREAM_NOT_EXIST.getMessage());
            }
        } else {*/
        TerminalService vmtService = terminalManageService.getFreeVmt();
        if (null == vmtService) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50008 : reach max join terminal numbers!");
            LogTools.debug(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"startCallDevice(GroupID:" + groupId + " ,account: " + mtAccount +") - [YYYY-MM-DDThh:mm:ss.SSSZ] failed Errcode:" + 50008 +"Error: reach max join terminal numbers!");
            p2PCallRequest.makeErrorResponseMsg(ConfInterfaceResult.REACH_MAX_JOIN_MTS.getCode(), HttpStatus.OK, ConfInterfaceResult.REACH_MAX_JOIN_MTS.getMessage());
            return;
        }
        vmtService.setGroupId(groupId);

        String waitMsg = P2PCallRequest.class.getName();
        vmtService.addWaitMsg(waitMsg, p2PCallRequest);
        p2PCallRequest.makeSuccessResponseMsg();
        if (!p2PCallParam.isDual()) {
            p2PCallRequest.setWaitMsg(new ArrayList<>(Arrays.asList(waitMsg, waitMsg, waitMsg, waitMsg)));
            if (vmtService.callMt(p2PCallParam)) {
                vmtService.setDualStream(true);
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "添加vmtService : " + vmtService.getE164());
                System.out.println("添加vmtService : " + vmtService.getE164());
                p2PCallGroup.addCallMember(mtAccount, vmtService);
            } else {
                vmtService.delWaitMsg(waitMsg);
                terminalManageService.freeVmt(vmtService.getE164());
                vmtService.setGroupId(null);
                TerminalStatusNotify terminalStatusNotify = new TerminalStatusNotify();
                TerminalStatus terminalStatus = new TerminalStatus(mtAccount, "MT", 0);
                terminalStatusNotify.addMtStatus(terminalStatus);
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "terminalService.getE164() " + mtAccount + ",terminalService.getGroupId() : " + groupId);
                System.out.println("terminalService.getE164() " + mtAccount + ",terminalService.getGroupId() : " + groupId);
                confInterfacePublishService.publishMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS, groupId, terminalStatusNotify);
                //p2PCallRequest.makeErrorResponseMsg(ConfInterfaceResult.P2PCALL.getCode(), HttpStatus.OK, ConfInterfaceResult.P2PCALL.getMessage());
            }
            //return;
        }
    }

    @Async("confTaskExecutor")
    public void cancelP2PCall(CancelP2PCallRequest cancelP2PCallRequest, CancelP2PCallParam cancelP2PCallParam) {
        String groupId = cancelP2PCallRequest.getGroupId();
        P2PCallGroup p2PCallGroup = p2pCallGroupMap.get(groupId);
        if (null == p2PCallGroup) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            System.out.println("50002 : group not exist!");
            LogTools.debug(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"stopCallDevice(GroupID:" + groupId + " ,account: " + cancelP2PCallParam.getAccount() +") - [YYYY-MM-DDThh:mm:ss.SSSZ] failed Errcode:" + 50002 +"Error: group not exist!");
            cancelP2PCallRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }

        String account = cancelP2PCallParam.getAccount();
        TerminalService vmtService = p2PCallGroup.getVmt(account);
        if (null == vmtService) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50015 : terminal not exist in this conference!");
            System.out.println("50015 : terminal not exist in this conference!");
            LogTools.debug(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"stopCallDevice(GroupID:" + groupId + " ,account: " + cancelP2PCallParam.getAccount() +") - [YYYY-MM-DDThh:mm:ss.SSSZ] failed Errcode:" + 50015 +"Error: terminal not exist in this conference!");
            cancelP2PCallRequest.makeErrorResponseMsg(ConfInterfaceResult.TERMINAL_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.TERMINAL_NOT_EXIST.getMessage());
            return;
        }

        if (cancelP2PCallParam.isDual()) {
            //停止双流呼叫
            ctrlVmtDualStream(vmtService, false, cancelP2PCallRequest);
            return;
        }

        //停止呼叫
        Boolean bOk = vmtService.cancelCallMt(vmtService);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "bOk : " + bOk);
        System.out.println("bOk : " + bOk);
        if (bOk) {
            vmtService.setGroupId(null);
            if(vmtService.dualSource.size() > 0){
                System.out.println("vmtService.dualSource.size() : " +vmtService.dualSource.size());
                vmtService.dualSource.remove(account);
            }
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "p2PCallGroup.removeCallMember : " + account);
            System.out.println("p2PCallGroup.removeCallMember : " + account);
            LogTools.debug(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"stopCallDevice(GroupID:"+ groupId +",account:"+ account+") - [YYYY-MM-DDThh:mm:ss.SSSZ] success");
            p2PCallGroup.removeCallMember(account);
            cancelP2PCallRequest.makeSuccessResponseMsg();
        } else {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50025 : p2p cancelCallMt failed!");
            LogTools.debug(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"stopCallDevice(GroupID:" + groupId + " ,account: " + cancelP2PCallParam.getAccount() +") - [YYYY-MM-DDThh:mm:ss.SSSZ] failed Errcode:" + 50025 +"Error: p2p cancelCallMt failed!");
            cancelP2PCallRequest.makeErrorResponseMsg(ConfInterfaceResult.P2PCANCELCALL.getCode(), HttpStatus.OK, ConfInterfaceResult.P2PCANCELCALL.getMessage());
        }

    }

    @Async("confTaskExecutor")
    public void statusNotify(ParticipantStatusNotify participantStatusNotify){
        String groupId = participantStatusNotify.getGroupID();
        String deviceId = participantStatusNotify.getDeviceID();
        int status = participantStatusNotify.getStatus();

        P2PCallGroup p2PCallGroup = p2pCallGroupMap.get(groupId);
        if (null == p2PCallGroup)
            return;

        TerminalService vmtService = p2PCallGroup.getVmt(deviceId);
        if (null == vmtService)
            return;

        if (status != 0)
            return;

        //挂断该呼叫
        //停止呼叫
        Boolean bOk = vmtService.cancelCallMt(vmtService);
        System.out.println("bOk : " + bOk);
        if (bOk) {
            vmtService.setGroupId(null);
            vmtService.setConfId(null);
            System.out.println("statusNotify, cancelCallMt");
            p2PCallGroup.removeCallMember(deviceId);
            p2pCallGroupMap.remove(groupId);
        } else {
            System.out.println("statusNotify, deviceId: " + deviceId + "in groupId( "+ groupId + ") has offline, cancelCall failed!!");
        }
    }

    public void p2pctrlDualStream(StartDualStreamRequest startDualStreamRequest, String mtE164, boolean b) {
        final String groupId = startDualStreamRequest.getGroupId();
        P2PCallGroup p2PCallGroup = p2pCallGroupMap.get(groupId);
        if (null == p2PCallGroup) {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50002 : group not exist!");
            startDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.GROUP_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.GROUP_NOT_EXIST.getMessage());
            return;
        }
        System.out.println("mtE164 : " + mtE164);
        TerminalService vmt = p2PCallGroup.getVmt(mtE164);
        if (null != vmt) {
            ctrlVmtDualStream(vmt, b, startDualStreamRequest);
        } else {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50026 : mainstream doesn't exist");
            startDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.MAINSTREAM_NOT_EXIST.getCode(), HttpStatus.OK, ConfInterfaceResult.MAINSTREAM_NOT_EXIST.getMessage());
        }
    }

    public boolean inspectionMt(GroupConfInfo groupConfInfo, String mode, String srcMtId, String dstMtId, InspectionRequest inspectionRequest) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "inspectionMt, mode:" + mode + ", srcMtId:" + srcMtId + ", dstMtId:" + dstMtId);
        System.out.println("inspectionMt, mode:" + mode + ", srcMtId:" + srcMtId + ", dstMtId:" + dstMtId);

        //需要将通道信息先添加,否则在订阅信息到来时,有可能还没有加入,出现消息无法正常移除的问题
        if (InspectionModeEnum.ALL.getName().equals(mode) || InspectionModeEnum.VIDEO.getName().equals(mode)) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "inspectionMt, add video channel!!!");
            System.out.println("inspectionMt, add video channel!!!");
            String channel = getInspectionChannel(groupConfInfo.getConfId(), dstMtId, InspectionModeEnum.VIDEO.getCode());  //视频
            inspectionRequest.addWaitMsg(channel);
            groupConfInfo.addWaitDealTask(channel, inspectionRequest);
        }

        if (InspectionModeEnum.ALL.getName().equals(mode) || InspectionModeEnum.AUDIO.getName().equals(mode)) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "inspectionMt, add audio channel!!!");
            System.out.println("inspectionMt, add audio channel!!!");
            String channel = getInspectionChannel(groupConfInfo.getConfId(), dstMtId, InspectionModeEnum.AUDIO.getCode()); //音频
            inspectionRequest.addWaitMsg(channel);
            groupConfInfo.addWaitDealTask(channel, inspectionRequest);
        }

        McuStatus mcuStatus = mcuRestClientService.inspections(groupConfInfo.getConfId(), mode, srcMtId, dstMtId);
        if (mcuStatus.getValue() == 0) {
            return true;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "inspectionMt, inspections failed!");
        System.out.println("inspectionMt, inspections failed!");
        List<String> channels = inspectionRequest.getWaitMsg();
        for (String channel : channels) {
            groupConfInfo.delWaitDealTask(channel);
        }
        inspectionRequest.setWaitMsg(null);
        LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50005 : inspection terminal failed!");
        inspectionRequest.makeErrorResponseMsg(ConfInterfaceResult.INSPECTION.getCode(), HttpStatus.OK, mcuStatus.getDescription());
        return false;
    }

    public McuStatus startInspectionForDiscusion(GroupConfInfo groupConfInfo, TerminalService mtService, TerminalService vmtService, boolean bMutual, JoinDiscussionGroupRequest joinDiscussionGroupRequest) {
        String confId = groupConfInfo.getConfId();
        String vmtMtId = vmtService.getMtId();
        String mtId = mtService.getMtId();

        String mtVideoChannel = getInspectionChannel(confId, mtId, InspectionModeEnum.VIDEO.getCode());
        joinDiscussionGroupRequest.addWaitMsg(mtVideoChannel);
        groupConfInfo.addWaitDealTask(mtVideoChannel, joinDiscussionGroupRequest);

        String mtAudioChannel = getInspectionChannel(confId, mtId, InspectionModeEnum.AUDIO.getCode());
        joinDiscussionGroupRequest.addWaitMsg(mtAudioChannel);
        groupConfInfo.addWaitDealTask(mtAudioChannel, joinDiscussionGroupRequest);

        boolean inspectAudioOk = false;
        McuStatus mcuStatus = mcuRestClientService.inspections(confId, InspectionModeEnum.AUDIO.getName(), vmtMtId, mtId);
        if (mcuStatus.getValue() == 0) {
            inspectAudioOk = true;
            mcuStatus = mcuRestClientService.inspections(confId, InspectionModeEnum.VIDEO.getName(), vmtMtId, mtId);
        }

        if (mcuStatus.getValue() > 0) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspectionForDiscusion, mt(" + mtService.getE164() + ") inspect  vmt(" + vmtService.getE164() + ") failed!");
            System.out.println("startInspectionForDiscusion, mt(" + mtService.getE164() + ") inspect  vmt(" + vmtService.getE164() + ") failed!");
            groupConfInfo.delWaitDealTask(mtVideoChannel);
            groupConfInfo.delWaitDealTask(mtAudioChannel);
            joinDiscussionGroupRequest.removeMsg(mtAudioChannel);
            joinDiscussionGroupRequest.removeMsg(mtVideoChannel);

            //只要会议终端存在一个方向选看失败，则需要清除相应的虚拟终端的选看及被选看资源
            vmtService.delInspentedTerminal(mtService.getE164());
            if (null != vmtService.getInspectionParam() && !vmtService.isInspection()) {
                //说明该vmt是刚选择进入讨论组的，此时，需要清楚选看参数
                vmtService.setInspectionParam(null);
            }

            if (null == vmtService.getInspectionParam() && vmtService.getInspentedTerminals().isEmpty()) {
                groupConfInfo.freeVmt(vmtService.getE164());
            }

            if (!inspectAudioOk) {
                mtService.setInspectionParam(null);
                mtService.delInspentedTerminal(vmtService.getE164());
            } else {
                mtService.setInspectVideoStatus(InspectionStatusEnum.FAIL.getCode());
                mcuRestClientService.cancelInspection(confId, InspectionModeEnum.AUDIO.getName(), mtId);
            }

            return mcuStatus;
        }

        if (!bMutual) {
            joinDiscussionGroupRequest.setIsStopInspection(mtService.getE164(), false);
            return mcuStatus;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspectionForDiscusion, vmt(" + vmtService.getE164() + ") start to inspect mt(" + mtService.getE164() + ")!");
        System.out.println("startInspectionForDiscusion, vmt(" + vmtService.getE164() + ") start to inspect mt(" + mtService.getE164() + ")!");

        String vmtVideoChannel = getInspectionChannel(confId, vmtMtId, InspectionModeEnum.VIDEO.getCode());
        joinDiscussionGroupRequest.addWaitMsg(vmtVideoChannel);
        groupConfInfo.addWaitDealTask(vmtVideoChannel, joinDiscussionGroupRequest);

        String vmtAudioChannel = getInspectionChannel(confId, vmtMtId, InspectionModeEnum.AUDIO.getCode());
        joinDiscussionGroupRequest.addWaitMsg(vmtAudioChannel);
        groupConfInfo.addWaitDealTask(vmtAudioChannel, joinDiscussionGroupRequest);

        inspectAudioOk = false;
        mcuStatus = mcuRestClientService.inspections(confId, InspectionModeEnum.AUDIO.getName(), mtId, vmtMtId);
        if (mcuStatus.getValue() == 0) {
            inspectAudioOk = true;
            mcuStatus = mcuRestClientService.inspections(confId, InspectionModeEnum.VIDEO.getName(), mtId, vmtMtId);
        }

        if (mcuStatus.getValue() > 0) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "startInspectionForDiscusion, vmt(" + vmtService.getE164() + ") inspect mt(" + mtService.getE164() + ") failed!");
            System.out.println("startInspectionForDiscusion, vmt(" + vmtService.getE164() + ") inspect mt(" + mtService.getE164() + ") failed!");

            groupConfInfo.delWaitDealTask(mtVideoChannel);
            groupConfInfo.delWaitDealTask(mtAudioChannel);
            joinDiscussionGroupRequest.removeMsg(mtAudioChannel);
            joinDiscussionGroupRequest.removeMsg(mtVideoChannel);
            groupConfInfo.delWaitDealTask(vmtVideoChannel);
            groupConfInfo.delWaitDealTask(vmtAudioChannel);
            joinDiscussionGroupRequest.removeMsg(vmtAudioChannel);
            joinDiscussionGroupRequest.removeMsg(vmtVideoChannel);

            if (!inspectAudioOk) {
                vmtService.setInspectionParam(null);
            } else {
                vmtService.setInspectVideoStatus(InspectionStatusEnum.FAIL.getCode());
                mcuRestClientService.cancelInspection(confId, InspectionModeEnum.AUDIO.getName(), vmtMtId);
            }

            //删除会议终端的被选看参数，并取消会议终端对虚拟终端的选看
            mtService.delInspentedTerminal(vmtService.getE164());
            mcuRestClientService.cancelInspection(confId, InspectionModeEnum.ALL.getName(), mtId);

            return mcuStatus;
        }

        joinDiscussionGroupRequest.setIsStopInspection(mtService.getE164(), true);

        return McuStatus.resolve(0);
    }

    public TerminalService chooseVmt(GroupConfInfo groupConfInfo, BaseRequestMsg<? extends BaseResponseMsg> requestMsg, boolean immediatelyResponse) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "chooseVmt, has no free vmt in group for discussion!");
        System.out.println("chooseVmt, has no free vmt in group for discussion!");
        if (groupConfInfo.reachMaxJoinMts()) {
            if (immediatelyResponse) {
                LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50008 : reach max join terminal numbers!");
                requestMsg.makeErrorResponseMsg(ConfInterfaceResult.REACH_MAX_JOIN_MTS.getCode(), HttpStatus.OK, ConfInterfaceResult.REACH_MAX_JOIN_MTS.getMessage());
            }
            return null;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "chooseVmt, start get vmt from free vmt list!");
        System.out.println("chooseVmt, start get vmt from free vmt list!");
        TerminalService vmtService = terminalManageService.getFreeVmt();
        if (null == vmtService) {
            if (immediatelyResponse) {
                LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50004 : reach max interface capacity!!");
                requestMsg.makeErrorResponseMsg(ConfInterfaceResult.NO_FREE_VMT.getCode(), HttpStatus.OK, ConfInterfaceResult.NO_FREE_VMT.getMessage());
            }
            return null;
        }

        String confId = groupConfInfo.getConfId();
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "chooseVmt, vmt start join conference! vmt:" + vmtService.getE164() + ", confId:" + confId);
        System.out.println("chooseVmt, vmt start join conference! vmt:" + vmtService.getE164() + ", confId:" + confId);
        vmtService.setGroupId(groupConfInfo.getGroupId());
        groupConfInfo.useVmt(vmtService);
        List<JoinConferenceRspMtInfo> mtInfos = joinConference(confId, vmtService);
        if (null == mtInfos) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "chooseVmt, vmt(" + vmtService.getE164() + ") join conference failed! confId:" + confId);
            System.out.println("chooseVmt, vmt(" + vmtService.getE164() + ") join conference failed! confId:" + confId);
            vmtService.setGroupId(null);
            groupConfInfo.delMember(vmtService);
            terminalManageService.freeVmt(vmtService.getE164());
            if (immediatelyResponse) {
                LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50005 : inspection terminal failed!");
                requestMsg.makeErrorResponseMsg(ConfInterfaceResult.INSPECTION.getCode(), HttpStatus.OK, ConfInterfaceResult.INSPECTION.getMessage());
            }
            return null;
        }

        terminalMediaSourceService.addGroupVmtMember(groupConfInfo.getGroupId(), vmtService.getE164());
        String channel = getTerminalInfoChannel(confId, 0, mtInfos.get(0).getMt_id());
        requestMsg.addWaitMsg(channel);
        groupConfInfo.addWaitDealTask(channel, requestMsg);
        groupConfInfo.addMtId(mtInfos.get(0).getMt_id(), vmtService.getE164());
        return vmtService;
    }

    public boolean joinDiscusssion(GroupConfInfo groupConfInfo, Terminal terminal, JoinDiscussionGroupRequest joinDiscussionGroupRequest) {
        //获取一个vmt，与mt进行一对一选看
        TerminalService vmtServiceForSrc = null;
        TerminalService vmtServiceForDst = null;
        TerminalService mtService = groupConfInfo.getMtMember(terminal.getMtE164());
        if (null == mtService)
            return false;

        //判断终端是否被选看
        InspectionSrcParam inspectionSrcParam = new InspectionSrcParam();
        inspectionSrcParam.setMode(InspectionModeEnum.ALL.getName());

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "joinDiscusssion, deal mt:" + mtService.getE164());
        System.out.println("joinDiscusssion, deal mt:" + mtService.getE164());
        InspectionSrcParam mtNowInspectionParam = mtService.getInspectionParam();
        if (null != mtNowInspectionParam) {
            //如果加入讨论组时，会议终端已经在选看，则认为会议终端正在忙，此时不能加入讨论组
            return false;
        }

        boolean bMutualInspection = true;
        ConcurrentHashMap<String, InspectedParam> inspectedParams = mtService.getInspentedTerminals();
        if (null != inspectedParams) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "joinDiscusssion, terminal is inspected!");
            System.out.println("joinDiscusssion, terminal is inspected!");
            for (ConcurrentHashMap.Entry<String, InspectedParam> inspectedParam : inspectedParams.entrySet()) {
                if (!inspectedParam.getValue().isVmt())
                    continue;

                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "joinDiscusssion, get vmt(" + inspectedParam.getKey() + ") which is inspecting terminal(" + terminal.getMtE164() + ")");
                System.out.println("joinDiscusssion, get vmt(" + inspectedParam.getKey() + ") which is inspecting terminal(" + terminal.getMtE164() + ")");
                TerminalService terminalService = groupConfInfo.findUsedVmt(inspectedParam.getKey());
                if (terminalService.getInspectionParam().getMode().equals(InspectionModeEnum.ALL.getName())) {
                    //判断vmt是否被别的会议终端选看
                    ConcurrentHashMap<String, InspectedParam> vmtInspectedParams = terminalService.getInspentedTerminals();
                    if (null == vmtInspectedParams || vmtInspectedParams.isEmpty()) {
                        //只有没有被任何终端选看的虚拟终端，才可以选择在讨论组中使用
                        vmtServiceForSrc = terminalService;
                    }

                    bMutualInspection = false;
                    vmtServiceForDst = terminalService;
                    break;
                }
            }

            if (null != vmtServiceForDst && null == vmtServiceForSrc) {
                //选看会议终端的虚拟终端已经被别的会议终端选看，在已经使用的VMT中选择一个没有被选看的虚拟终端
                vmtServiceForSrc = groupConfInfo.getNotBeInspectedTerminalServiceFromUsedVmtMember();
            }
        }

        boolean bInspection = true;
        if (null == vmtServiceForDst || null == vmtServiceForSrc) {
            int freeVmtNums = groupConfInfo.getFreeVmtMemberNum();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "joinDiscusssion, null == vmtService, start choose free vmt for discussion! mt:" + mtService.getE164() + ", freeVmtNum:" + freeVmtNums);
            System.out.println("joinDiscusssion, null == vmtService, start choose free vmt for discussion! mt:" + mtService.getE164() + ", freeVmtNum:" + freeVmtNums);
            TerminalService vmtService = groupConfInfo.getAndUseVmt(null);
            if (null == vmtService) {
                if (freeVmtNums > 0) {
                    //说明vmt还没有成功加入会议，需要等待
                    return false;
                }

                vmtService = chooseVmt(groupConfInfo, joinDiscussionGroupRequest, false);
                if (null == vmtService) {
                    return false;
                }
                bInspection = false;  //等待加入会议成功之后，再进行相互选看
            }

            if (null == vmtServiceForDst) {
                //如果vmtServiceForDst为空，说明加入讨论组的会议终端没有被任何虚拟终端选看，此时
                //的vmtServiceForSrc也就一定为NULL
                vmtServiceForDst = vmtService;
                vmtServiceForSrc = vmtService;

                //设置vmt的选看参数
                InspectionSrcParam vmtInspectionParam = new InspectionSrcParam();
                vmtInspectionParam.setMode(InspectionModeEnum.ALL.getName());
                vmtInspectionParam.setMtE164(mtService.getE164());
                vmtServiceForDst.setInspectionParam(vmtInspectionParam);
                vmtServiceForDst.setInspectionStatus(InspectionStatusEnum.UNKNOWN);
                vmtServiceForDst.setInspectVideoStatus(InspectionStatusEnum.UNKNOWN.getCode());
                vmtServiceForDst.setInspectAudioStatus(InspectionStatusEnum.UNKNOWN.getCode());

                //设置mt的被选看参数
                InspectedParam mtInspectedParam = new InspectedParam();
                mtInspectedParam.setVmt(true);
                mtService.addInspentedTerminal(vmtServiceForDst.getE164(), mtInspectedParam);
            } else {
                //如果vmtServiceForDst不为空，说明加入讨论组的会议终端已经被一个虚拟终端选看
                //但是选看该会议终端的虚拟终端已经被其他会议终端选看，不能作为讨论组成员
                vmtServiceForSrc = vmtService;
            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "joinDiscusssion, startInspection to set inspection param! vmt for discussion, e164:" + vmtService.getE164());
            System.out.println("joinDiscusssion, startInspection to set inspection param! vmt for discussion, e164:" + vmtService.getE164());
        }

        if (null == vmtServiceForSrc.getInspectedParam(mtService.getE164())) {
            //设置vmt的被选看参数
            InspectedParam vmtInspectedParam = new InspectedParam();
            vmtInspectedParam.setVmt(false);
            vmtServiceForSrc.addInspentedTerminal(mtService.getE164(), vmtInspectedParam);
        }

        InspectionSrcParam mtInspectionParam = new InspectionSrcParam();
        mtInspectionParam.setMode(InspectionModeEnum.ALL.getName());
        mtInspectionParam.setMtE164(vmtServiceForSrc.getE164());
        mtService.setInspectionParam(mtInspectionParam);
        mtService.setInspectionStatus(InspectionStatusEnum.UNKNOWN);
        mtService.setInspectVideoStatus(InspectionStatusEnum.UNKNOWN.getCode());
        mtService.setInspectAudioStatus(InspectionStatusEnum.UNKNOWN.getCode());

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "joinDiscusssion, bInspection : " + bInspection);
        System.out.println("joinDiscusssion, bInspection : " + bInspection);
        if (!bInspection)
            return true;

        McuStatus mcuStatus = startInspectionForDiscusion(groupConfInfo, mtService, vmtServiceForSrc, bMutualInspection, joinDiscussionGroupRequest);
        if (mcuStatus.getValue() > 0)
            return false;

        return true;
    }

    @Async("confTaskExecutor")
    public void testString(DeferredResult<String> deferredResult) {

        logger.info(Thread.currentThread().getName() + "进入ConfInterfaceService的testString方法");

        try {
            TimeUnit.SECONDS.sleep(10);
            deferredResult.setResult("ok on testString");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Async("confTaskExecutor")
    public void testString1(TestStringRequest deferredResult) {

        logger.info(Thread.currentThread().getName() + "进入ConfInterfaceService的testString1方法");

        try {
            TimeUnit.SECONDS.sleep(2);
            List<String> strings = new ArrayList<>();
            strings.add("hello, good afternoon");
            strings.add("i am testString1");
            deferredResult.setTestStrings(strings);
            deferredResult.makeSuccessResponseMsg();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getTerminalInfoChannel(String confId, int cascade_id, String mtId) {
        //终端列表通道 /confs/{conf_id}/cascades/{cascade_id}/mts/{mt_id}
        StringBuilder channel = new StringBuilder();

        channel.append("/confs/");
        channel.append(confId);
        channel.append("/cascades/");
        channel.append(cascade_id);
        channel.append("/mts/");
        channel.append(mtId);

        return channel.toString();
    }

    private String getSpeakerChannel(String confId) {
        // 订阅通道/confs/{conf_id}/speaker
        StringBuilder channel = new StringBuilder();

        channel.append("/confs/");
        channel.append(confId);
        channel.append("/speaker");
        return channel.toString();
    }

    private String getInspectionChannel(String confId, String mtId, int mode) {
        StringBuilder channel = new StringBuilder();

        channel.append("/confs/");
        channel.append(confId);
        channel.append("/inspections/");
        channel.append(mtId);
        channel.append("/");
        channel.append(mode);
        return channel.toString();
    }

    private String getDualStreamChannel(String confId) {
        StringBuilder channel = new StringBuilder();

        channel.append("/confs/");
        channel.append(confId);
        channel.append("/dualstream");

        return channel.toString();
    }

    private List<JoinConferenceRspMtInfo> joinConference(String confId, TerminalService terminalService) {
        List<Terminal> joinConfVmts = new ArrayList<>();
        Terminal terminal = new Terminal(terminalService.getE164());
        joinConfVmts.add(terminal);

        return mcuRestClientService.joinConference(confId, joinConfVmts);
    }

    private void ctrlMtDualStream(TerminalService mtService, boolean dual, GroupConfInfo groupConfInfo, BaseRequestMsg ctrlDualStreamRequest) {
        String confId = groupConfInfo.getConfId();
        String dualStreamChannel = getDualStreamChannel(confId);
        ctrlDualStreamRequest.addWaitMsg(dualStreamChannel);
        groupConfInfo.addWaitDealTask(dualStreamChannel, ctrlDualStreamRequest);

        McuStatus mcuStatus = mcuRestClientService.ctrlDualStream(confId, mtService.getMtId(), dual);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ctrlMtDualStream, dual:" + dual + ", errorCode:" + mcuStatus.getValue() + ", errMsg:" + mcuStatus.getDescription());
        System.out.println("ctrlMtDualStream, dual:" + dual + ", errorCode:" + mcuStatus.getValue() + ", errMsg:" + mcuStatus.getDescription());

        if (mcuStatus.getValue() == McuStatus.OK.getValue()) {
            return;
        }

        ctrlDualStreamRequest.removeMsg(dualStreamChannel);
        groupConfInfo.delWaitDealTask(dualStreamChannel);
        LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50019 : control dual stream failed!");
        ctrlDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.CTRL_DUALSTREAM.getCode(), HttpStatus.OK, mcuStatus.getDescription());
    }

    private void ctrlVmtDualStream(TerminalService vmtService, boolean dual, BaseRequestMsg ctrlDualStreamRequest) {
        if (dual) {
            vmtService.openDualStreamChannel(ctrlDualStreamRequest);
        } else {
            boolean bOk = vmtService.closeDualStreamChannel();
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "bOk : " + bOk);
            System.out.println("bOk : " + bOk);
            if (bOk) {
                List<DetailMediaResouce> mediaResouces = vmtService.getForwardChannel();
                TerminalMediaResource oldTerminalMediaResource = terminalMediaSourceService.getTerminalMediaResource(vmtService.getE164());
                oldTerminalMediaResource.setForwardResources(TerminalMediaResource.convertToMediaResource(mediaResouces, "all"));
                terminalMediaSourceService.setTerminalMediaResource(oldTerminalMediaResource);
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ctrlVmtDualStream, closeDualStreamChannel succeed");
                System.out.println("ctrlVmtDualStream, closeDualStreamChannel succeed");
                ctrlDualStreamRequest.makeSuccessResponseMsg();
            } else {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ctrlVmtDualStream, closeDualStreamChannel failed!");
                System.out.println("ctrlVmtDualStream, closeDualStreamChannel failed!");
                LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "50023 : close logical channel failed!");
                ctrlDualStreamRequest.makeErrorResponseMsg(ConfInterfaceResult.CLOSE_CHANNEL.getCode(), HttpStatus.OK, ConfInterfaceResult.CLOSE_CHANNEL.getMessage());
            }
        }
    }

    /*
        public void confTimeout(DeferredResult<ResponseEntity<BaseResponseMsg>> deferredResult){
            deferredResult.onTimeout(new Runnable() {
                @Override
                public void run() {
                    logger.info(Thread.currentThread().getName()+"confTimeout");
                    BaseResponseMsg baseResponseMsg = new BaseResponseMsg();
                    baseResponseMsg.error(400, "timeout");
                    ResponseEntity<BaseResponseMsg> baseResponseMsgResponseEntity = new ResponseEntity<BaseResponseMsg>(baseResponseMsg, HttpStatus.OK);
                    deferredResult.setErrorResult(baseResponseMsgResponseEntity);
                }
            });
        }
    */

    @Autowired
    private TerminalManageService terminalManageService;

    @Autowired
    private McuRestClientService mcuRestClientService;

    @Autowired
    private TerminalMediaSourceService terminalMediaSourceService;

    @Autowired
    private ConfInterfacePublishService confInterfacePublishService;

    @Autowired
    private BaseSysConfig baseSysConfig;

    private Map<String, GroupConfInfo> groupConfInfoMap = new ConcurrentHashMap<>();
    private Map<String, String> confGroupMap = new ConcurrentHashMap<>();
    public static Map<String, P2PCallGroup> p2pCallGroupMap = new ConcurrentHashMap<>();


}
