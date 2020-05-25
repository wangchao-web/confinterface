package com.kedacom.confinterface.controller;

import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dto.*;
import com.kedacom.confinterface.inner.SubscribeMsgTypeEnum;
import com.kedacom.confinterface.service.ConfInterfaceInitializingService;
import com.kedacom.confinterface.service.ConfInterfaceService;
import com.kedacom.confinterface.service.ConfInterfacePublishService;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.Valid;

@RestController
@RequestMapping("/services/confinterface/v1")
public class ConfInterfaceController {

    @Autowired
    private ConfInterfaceService confInterfaceService;

    @Autowired
    private ConfInterfacePublishService confInterfacePublishService;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    public ConfInterfaceController(ConfInterfaceService confInterfaceService) {

        this.confInterfaceService = confInterfaceService;
    }

    @PostMapping(value = "/participants")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> joinConference(@RequestParam("GroupId") String groupId, @Valid @RequestBody JoinConferenceParam joinConferenceParam) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in joinConference, groupId : " + groupId + ", time : " + System.currentTimeMillis() );
        System.out.println("now in joinConference, groupId : " + groupId + "joinConferenceParam.getConfinterface() : " +joinConferenceParam.getConfinterface() + ", time : " + System.currentTimeMillis());
        JoinConferenceRequest joinConferenceRequest = new JoinConferenceRequest(groupId, joinConferenceParam.getMts(),joinConferenceParam.getConfinterface());
        confInterfaceService.joinConference(joinConferenceRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished joinConference, time : " + System.currentTimeMillis());
        System.out.println("now finished joinConference, time : " + System.currentTimeMillis());
        return joinConferenceRequest.getResponseMsg();
    }

    @DeleteMapping(value = "/participants")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> leftConference(@RequestParam("GroupId") String groupId, @Valid @RequestBody LeftConferenceParam leftConferenceParam) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in leftConference, groupId: " + groupId + ", cancelGroup: " + leftConferenceParam.isCancelGroup() + ", time : " + System.currentTimeMillis());
        System.out.println("now in leftConference, groupId: " + groupId + ", cancelGroup: " + leftConferenceParam.isCancelGroup() + ", time : " + System.currentTimeMillis());
        LeftConferenceRequest leftConferenceRequest = new LeftConferenceRequest(groupId, leftConferenceParam);
        confInterfaceService.leftConference(leftConferenceRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished leftConference, time : " + System.currentTimeMillis());
        System.out.println("now finished leftConference, time : " + System.currentTimeMillis());
        return leftConferenceRequest.getResponseMsg();
    }

    @PostMapping(value = "/broadcast")
    public DeferredResult<ResponseEntity<BroadCastResponse>> setBroadcastSrc(@RequestParam("GroupId") String groupId, @Valid @RequestBody BroadCastParam broadCastParam) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in setBroadcastSrc, groupId: " + groupId + ", broadCastParam:" + broadCastParam + ", time : " + System.currentTimeMillis());
        System.out.println("now in setBroadcastSrc, groupId: " + groupId + ", broadCastParam:" + broadCastParam + ", time : " + System.currentTimeMillis());
        BroadCastRequest broadCastRequest = new BroadCastRequest(groupId, broadCastParam);
        confInterfaceService.setBroadcastSrc(broadCastRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished setBroadcastSrc, time : " + System.currentTimeMillis());
        System.out.println("now finished setBroadcastSrc, time : " + System.currentTimeMillis());
        return broadCastRequest.getResponseMsg();
    }

    @DeleteMapping(value = "/broadcast")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> delBroadcastSrc(@RequestParam("GroupId") String groupId) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in delBroadcastSrc, groupId: " + groupId + ", time : " + System.currentTimeMillis());
        System.out.println("now in delBroadcastSrc, groupId: " + groupId + ", time : " + System.currentTimeMillis());
        CancelBroadCastRequest cancelBroadCastRequest = new CancelBroadCastRequest(groupId);
        confInterfaceService.cancelBroadcast(cancelBroadCastRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished delBroadcastSrc, time : " + System.currentTimeMillis());
        System.out.println("now finished delBroadcastSrc, time : " + System.currentTimeMillis());
        return cancelBroadCastRequest.getResponseMsg();
    }

    @PostMapping(value = "/discussiongroup")
    public DeferredResult<ResponseEntity<JoinDisscussionGroupResponse>> joinDiscussionGroup(@RequestParam("GroupId") String groupId, @Valid @RequestBody JoinDisscussionGroupParam joinDisscussionGroupParam) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in joinDiscussionGroup, groupId: " + groupId + ", time : " + System.currentTimeMillis());
        System.out.println("now in joinDiscussionGroup, groupId: " + groupId + ", time : " + System.currentTimeMillis());
        JoinDiscussionGroupRequest joinDiscussionGroupRequest = new JoinDiscussionGroupRequest(groupId, joinDisscussionGroupParam.getMts());
        confInterfaceService.joinDiscussionGroup(joinDiscussionGroupRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished joinDiscussionGroup, time : " + System.currentTimeMillis());
        System.out.println("now finished joinDiscussionGroup, time : " + System.currentTimeMillis());
        return joinDiscussionGroupRequest.getResponseMsg();
    }

    @DeleteMapping(value = "/discussiongroup")
    public DeferredResult<ResponseEntity<LeftDiscussionGroupResponse>> leftDiscussionGroup(@RequestParam("GroupId") String groupId, @Valid @RequestBody LeftDiscussionGroupParam leftDisscussionGroupParam) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in leftDiscussionGroup, groupId: " + groupId + ", time : " + System.currentTimeMillis());
        System.out.println("now in leftDiscussionGroup, groupId: " + groupId + ", time : " + System.currentTimeMillis());
        LeftDiscussionGroupRequest leftDiscussionGroupRequest = new LeftDiscussionGroupRequest(groupId, leftDisscussionGroupParam.getMts());
        confInterfaceService.leftDiscussionGroup(leftDiscussionGroupRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished leftDiscussionGroup, time : " + System.currentTimeMillis());
        System.out.println("now finished leftDiscussionGroup, time : " + System.currentTimeMillis());
        return leftDiscussionGroupRequest.getResponseMsg();
    }

    @PostMapping(value = "/inspections")
    public DeferredResult<ResponseEntity<InspectionResponse>> inspection(@RequestParam("GroupId") String groupId, @Valid @RequestBody InspectionParam inspectionParam) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in inspection, groupId: " + groupId +", InspectionParam:"+ inspectionParam + ", time : " + System.currentTimeMillis());
        System.out.println("now in inspection, groupId: " + groupId +", InspectionParam:"+ inspectionParam + ", time : " + System.currentTimeMillis());
        InspectionRequest inspectionRequest = new InspectionRequest(groupId, inspectionParam);
        confInterfaceService.startInspection(inspectionRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished inspection, time : " + System.currentTimeMillis());
        System.out.println("now finished inspection, time : " + System.currentTimeMillis());
        return inspectionRequest.getResponseMsg();
    }

    @DeleteMapping(value = "/inspections")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> cancelInspection(@RequestParam("GroupId") String groupId, @Valid @RequestBody InspectionParam inspectionParam) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in cancelInspection, groupId: " + groupId + ", cancelInspectionParam:" + inspectionParam + ", time : " + System.currentTimeMillis());
        System.out.println("now in cancelInspection, groupId: " + groupId + ", cancelInspectionParam:" + inspectionParam + ", time : " + System.currentTimeMillis());
        CancelInspectionRequest cancelInspectionRequest = new CancelInspectionRequest(groupId, inspectionParam);
        confInterfaceService.cancelInspection(cancelInspectionRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished cancelInspection, time : " + System.currentTimeMillis());
        System.out.println("now finished cancelInspection, time : " + System.currentTimeMillis());
        return cancelInspectionRequest.getResponseMsg();
    }

    @PostMapping(value = "/cameracontrol")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> ctrlCamera(@RequestParam("GroupId") String groupId, @Valid @RequestBody CameraCtrlParam cameraCtrlParam) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in ctrlCamera, groupId: " + groupId + ", CameraCtrlParam:" + cameraCtrlParam + ", time : " + System.currentTimeMillis());
        System.out.println("now in ctrlCamera, groupId: " + groupId + ", CameraCtrlParam:" + cameraCtrlParam + ", time : " + System.currentTimeMillis());
        CameraCtrlRequest cameraCtrlRequest = new CameraCtrlRequest(groupId, cameraCtrlParam);
        confInterfaceService.ctrlCamera(cameraCtrlRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished ctrlCamera, time : " + System.currentTimeMillis());
        System.out.println("now finished ctrlCamera, time : " + System.currentTimeMillis());
        return cameraCtrlRequest.getResponseMsg();
    }

    @PostMapping(value = "/subscription/terminalstatus")
    public ResponseEntity<BaseResponseMsg> subscribeTerminalStatus(@RequestParam("GroupId") String groupId, @Valid @RequestBody SubscribeTerminalStatusParam subscribeTerminalStatusParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in subscribeTerminalStatus, groupId:"+groupId+", subscribeParam:"+subscribeTerminalStatusParam);
        System.out.println("now in subscribeTerminalStatus, groupId:"+groupId+", subscribeParam:"+subscribeTerminalStatusParam);
        if (null != confInterfacePublishService) {
            confInterfacePublishService.addSubscribeMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS.getType(), groupId, subscribeTerminalStatusParam.getUrl());
            BaseResponseMsg baseResponseMsg = new BaseResponseMsg(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
            return new ResponseEntity<>(baseResponseMsg, HttpStatus.OK);
        } else {
            BaseResponseMsg baseResponseMsg = new BaseResponseMsg(ConfInterfaceResult.NOT_SUPPORT_METHOD.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.NOT_SUPPORT_METHOD.getMessage());
            return new ResponseEntity<>(baseResponseMsg, HttpStatus.OK);
        }
    }

    @DeleteMapping(value = "/subscription/terminalstatus")
    public ResponseEntity<BaseResponseMsg> cancelSubscribeTerminalStatus(@RequestParam("GroupId") String groupId, @Valid @RequestBody SubscribeTerminalStatusParam subscribeTerminalStatusParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in cancelSubscribeTerminalStatus, groupId:"+groupId+", subscribeParam:"+subscribeTerminalStatusParam);
        System.out.println("now in cancelSubscribeTerminalStatus, groupId:"+groupId+", subscribeParam:"+subscribeTerminalStatusParam);
        if (null != confInterfacePublishService) {
            confInterfacePublishService.cancelSubscribeMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS.getType(), groupId, subscribeTerminalStatusParam.getUrl());
            BaseResponseMsg baseResponseMsg = new BaseResponseMsg(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
            return new ResponseEntity<>(baseResponseMsg, HttpStatus.OK);
        } else {
            BaseResponseMsg baseResponseMsg = new BaseResponseMsg(ConfInterfaceResult.NOT_SUPPORT_METHOD.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.NOT_SUPPORT_METHOD.getMessage());
            return new ResponseEntity<>(baseResponseMsg, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/iframe")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> sendIFrame(@RequestParam("GroupId") String groupId, @Valid @RequestBody SendIFrameParam sendIFrameParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in sendIFrame, groupId:"+groupId+", iframeParam:"+sendIFrameParam.toString());
        System.out.println("now in sendIFrame, groupId:"+groupId+", iframeParam:"+sendIFrameParam.toString());
        SendIFrameRequest sendIFrameRequest = new SendIFrameRequest(groupId, sendIFrameParam);
        confInterfaceService.sendIFrame(sendIFrameRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished sendIFrame, time:"+System.currentTimeMillis());
        System.out.println("now finished sendIFrame, time:"+System.currentTimeMillis());
        return sendIFrameRequest.getResponseMsg();
    }

    @PostMapping(value = "/mts/{mtE164}/volume")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> ctrlVolume(@RequestParam("GroupId") String groupId, @PathVariable String mtE164, @Valid @RequestBody CtrlVolumeParam ctrlVolumeParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in ctrlVolume, groupId:"+groupId + ", mtE164:" + mtE164 +", ctrlVolumeParam:"+ctrlVolumeParam);
        System.out.println("now in ctrlVolume, groupId:"+groupId + ", mtE164:" + mtE164 +", ctrlVolumeParam:"+ctrlVolumeParam);
        CtrlVolumeRequest ctrlVolumeRequest = new CtrlVolumeRequest(groupId, mtE164, ctrlVolumeParam);
        confInterfaceService.ctrlVolume(ctrlVolumeRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished ctrlVolume, time:"+System.currentTimeMillis());
        System.out.println("now finished ctrlVolume, time:"+System.currentTimeMillis());
        return ctrlVolumeRequest.getResponseMsg();
    }

    @PostMapping(value = "/mts/{mtE164}/silence")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> silence(@RequestParam("GroupId") String groupId, @PathVariable String mtE164, @Valid @RequestBody SilenceOrMuteParam silenceParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in setSilence, groupId:"+groupId + ", mtE164:" + mtE164 +", silenceParam:"+silenceParam);
        System.out.println("now in setSilence, groupId:"+groupId + ", mtE164:" + mtE164 +", silenceParam:"+silenceParam);
        return silenceOrMute(groupId, mtE164, true, silenceParam);
    }

    @PostMapping(value = "/mts/{mtE164}/mute")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> mute(@RequestParam("GroupId") String groupId, @PathVariable String mtE164, @Valid @RequestBody SilenceOrMuteParam muteParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in setMute, groupId:"+groupId + ", mtE164:" + mtE164 +", muteParam:"+muteParam);
        System.out.println("now in setMute, groupId:"+groupId + ", mtE164:" + mtE164 +", muteParam:"+muteParam);
        return silenceOrMute(groupId, mtE164, false, muteParam);
    }

    @PostMapping(value = "/dualStream")
    public DeferredResult<ResponseEntity<StartDualResponse>> startDual(@RequestParam("GroupId") String groupId, @Valid @RequestBody DualStreamParam dualStreamParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in startDual, groupId:"+groupId + ", dualStreamParam:" + dualStreamParam);
        System.out.println("now in startDual, groupId:"+groupId + ", dualStreamParam:" + dualStreamParam);
        StartDualStreamRequest startDualStreamRequest = new StartDualStreamRequest(groupId, dualStreamParam);
        confInterfaceService.ctrlDualStream(startDualStreamRequest, dualStreamParam.getMtE164(), true);
        return startDualStreamRequest.getResponseMsg();
    }

    @DeleteMapping(value = "/dualStream")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> stopDual(@RequestParam("GroupId") String groupId, @Valid @RequestBody DualStreamParam dualStreamParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in stopDual, groupId:"+groupId + ", dualStreamParam:" + dualStreamParam);
        System.out.println("now in stopDual, groupId:"+groupId + ", dualStreamParam:" + dualStreamParam);
        CancelDualStreamRequest cancelDualStreamRequest = new CancelDualStreamRequest(groupId, dualStreamParam);
        confInterfaceService.ctrlDualStream(cancelDualStreamRequest, dualStreamParam.getMtE164(), false);
        return cancelDualStreamRequest.getResponseMsg();
    }

    @GetMapping(value = "/dualStream")
    public DeferredResult<ResponseEntity<QueryDualStreamResponse>> queryDualStream(@RequestParam("GroupId") String groupId){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in queryDualStream, groupId:"+groupId);
        System.out.println("now in queryDualStream, groupId:"+groupId);
        QueryDualStreamRequest queryDualStreamRequest = new QueryDualStreamRequest(groupId);
        confInterfaceService.queryDualStream(queryDualStreamRequest);
        return queryDualStreamRequest.getResponseMsg();
    }

    @GetMapping(value = "/vmts")
    public DeferredResult<ResponseEntity<QueryVmtsResponse>> getVmts(){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"query all vmts! time:"+System.currentTimeMillis());
        System.out.println("query all vmts! time:"+System.currentTimeMillis());
        QueryVmtsRequest queryVmtsRequest = new QueryVmtsRequest();
        confInterfaceService.queryVmts(queryVmtsRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished query all vmts, time:"+System.currentTimeMillis());
        System.out.println("now finished query all vmts, time:"+System.currentTimeMillis());
        return queryVmtsRequest.getResponseMsg();
    }

    @PostMapping(value = "p2pcall")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> p2pCall(@RequestParam("GroupId") String groupId, @Valid @RequestBody P2PCallParam p2PCallParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "now in p2pCall, groupId:"+groupId+", p2pCallParam:"+p2PCallParam);
        System.out.println("now in p2pCall, groupId:"+groupId+", p2pCallParam:"+p2PCallParam);
        LogTools.debug(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"startCallDevice(GroupID: " + groupId + ", account: "+ p2PCallParam.getAccount() +") - [YYYY-MM-DDThh:mm:ss.SSSZ] start");
        P2PCallRequest p2PCallRequest = new P2PCallRequest(groupId, p2PCallParam.getAccount());
        confInterfaceService.p2pCall(p2PCallRequest, p2PCallParam);
        return p2PCallRequest.getResponseMsg();
    }

    @DeleteMapping(value = "p2pcall")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> cancelP2PCall(@RequestParam("GroupId") String groupId, @Valid @RequestBody CancelP2PCallParam cancelP2PCallParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"cancel p2pCall, groupId:"+groupId+", p2pCallParam:"+cancelP2PCallParam);
        System.out.println("cancel p2pCall, groupId:"+groupId+", p2pCallParam:"+cancelP2PCallParam);
        LogTools.debug(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"stopCallDevice(GroupID: " + groupId + ", account: "+ cancelP2PCallParam.getAccount() +") - [YYYY-MM-DDThh:mm:ss.SSSZ] start");
        CancelP2PCallRequest cancelP2PCallRequest = new CancelP2PCallRequest(groupId);
        confInterfaceService.cancelP2PCall(cancelP2PCallRequest, cancelP2PCallParam);
        return cancelP2PCallRequest.getResponseMsg();
    }

    @PostMapping(value = "/p2pdualStream")
    public DeferredResult<ResponseEntity<StartDualResponse>> p2pStartDual(@RequestParam("GroupId") String groupId, @Valid @RequestBody DualStreamParam dualStreamParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in p2pstartDual, groupId:"+groupId + ", dualStreamParam:" + dualStreamParam);
        System.out.println("now in p2pstartDual, groupId:"+groupId + ", dualStreamParam:" + dualStreamParam);
        StartDualStreamRequest startDualStreamRequest = new StartDualStreamRequest(groupId, dualStreamParam);
        confInterfaceService.p2pctrlDualStream(startDualStreamRequest, dualStreamParam.getMtE164(), true);
        return startDualStreamRequest.getResponseMsg();
    }

    @GetMapping(value = "/version")
    public String queryVersion(){
        return ConfInterfaceInitializingService.VERSION;
    }

    private DeferredResult<ResponseEntity<BaseResponseMsg>> silenceOrMute(String groupId, String mtE164, boolean silence, SilenceOrMuteParam silenceOrMuteParam){
        CtrlSilenceOrMuteRequest ctrlSilenceOrMuteRequest = new CtrlSilenceOrMuteRequest(groupId, mtE164, silence, silenceOrMuteParam);
        confInterfaceService.silenceOrMute(ctrlSilenceOrMuteRequest);
        return ctrlSilenceOrMuteRequest.getResponseMsg();
    }

    @PostMapping(value = "/participants/statusnotify")
    public ResponseEntity<BaseResponseMsg> statusNotify(@RequestBody ParticipantStatusNotify statusNotify){
        confInterfaceService.statusNotify(statusNotify);

        BaseResponseMsg baseResponseMsg = new BaseResponseMsg(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        return new ResponseEntity<>(baseResponseMsg, HttpStatus.OK);
    }

    @GetMapping(value = "/confs")
    public DeferredResult<ResponseEntity<QueryAllConfsResponse>> getConfs(){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"query All confs! time:"+System.currentTimeMillis());
        System.out.println("query All confs ! time:"+System.currentTimeMillis());
        QueryAllConfsRequest queryAllConfsRequest = new QueryAllConfsRequest();
        confInterfaceService.queryConfs(queryAllConfsRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished query All confs, time:"+System.currentTimeMillis());
        System.out.println("now finished query All confs ! time, time:"+System.currentTimeMillis());
        return queryAllConfsRequest.getResponseMsg();
    }

    @GetMapping(value = "/confInfo")
    public DeferredResult<ResponseEntity<QueryConfInfoResponse>>  getConfInfo(@RequestParam("GroupId") String groupId){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"query confInfo! time:"+System.currentTimeMillis());
        System.out.println("query confInfo ! time:"+System.currentTimeMillis());
        QueryConfInfoRequest queryConfInfoRequest = new QueryConfInfoRequest(groupId);
        confInterfaceService.queryConfInfo(queryConfInfoRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished  query confInfo, time:"+System.currentTimeMillis());
        System.out.println("now finished query confInfo, time:"+System.currentTimeMillis());
        return queryConfInfoRequest.getResponseMsg();
    }

    @GetMapping(value = "/cascades")
    public DeferredResult<ResponseEntity<QueryConfCascadesInfoResponse>>  getConfCascadeInfo(@RequestParam("GroupId") String groupId){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"query getConfCascadeInfo! time:"+System.currentTimeMillis());
        System.out.println("query getConfCascadeInfo ! time:"+System.currentTimeMillis());
        QueryConfsCascadesRequest queryConfsCascadesRequest = new QueryConfsCascadesRequest(groupId);
        confInterfaceService.queryConfsCascades(queryConfsCascadesRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished query query getConfCascadeInfo, time:"+System.currentTimeMillis());
        System.out.println("now finished query getConfCascadeInfo, time:"+System.currentTimeMillis());
        return queryConfsCascadesRequest.getResponseMsg();
    }

    @GetMapping(value = "/cascades/{cascadeId}/mts")
    public DeferredResult<ResponseEntity<QueryConfsCascadesMtsResponse>> getConfsCascadesMts(@RequestParam("GroupId") String groupId, @PathVariable String cascadeId){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"query ConfsCascadesMts! time:"+System.currentTimeMillis());
        System.out.println("query ConfsCascadesMts ! time:"+System.currentTimeMillis());
        QueryConfsCascadesMtsRequest queryConfsCascadesMtsRequest = new QueryConfsCascadesMtsRequest(groupId,cascadeId);
        confInterfaceService.queryConfsCascadesMts(queryConfsCascadesMtsRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished query ConfsCascadesMts, time:"+System.currentTimeMillis());
        System.out.println("now query ConfsCascadesMts, time:"+System.currentTimeMillis());
        return queryConfsCascadesMtsRequest.getResponseMsg();
    }


    @PostMapping(value = "/sms")
    public DeferredResult<ResponseEntity<SendSmsResponse>> sendSms(@RequestParam("GroupId") String groupId, @Valid @RequestBody SendSmsParam sendSmsParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in sendSms, groupId : " + groupId + "sendSmsParam.toString() : " +sendSmsParam.toString() + ", time : " + System.currentTimeMillis());
        System.out.println("now in sendSms, groupId : " + groupId + "sendSmsParam.toString() : " +sendSmsParam.toString() + ", time : " + System.currentTimeMillis());
        SendSmsRequest sendSmsRequest = new SendSmsRequest(groupId, sendSmsParam);
        confInterfaceService.sendSms(sendSmsRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished sendSms, time : " + System.currentTimeMillis());
        System.out.println("now finished sendSms, time : " + System.currentTimeMillis());
        return sendSmsRequest.getResponseMsg();

    }


    @GetMapping(value = "/confs/mt/{mtE164}")
    public DeferredResult<ResponseEntity<QueryConfMtInfoResponse>> getConfMtInfo(@RequestParam("GroupId") String groupId, @PathVariable String mtE164){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"get confs mt info ! time:"+System.currentTimeMillis());
        System.out.println("get confs mt info ! time:"+System.currentTimeMillis());
        GetConfMtRequest getConfMtRequest = new GetConfMtRequest(groupId,mtE164);
        confInterfaceService.queryConfMtInfo(getConfMtRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished get confs mt info ! time:"+System.currentTimeMillis());
        System.out.println("now finished get confs mt info ! time:"+System.currentTimeMillis());
        return getConfMtRequest.getResponseMsg();
    }

    @PostMapping(value = "/vmps")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> startVmps(@RequestParam("GroupId") String groupId, @Valid @RequestBody VmpsParam vmpsParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in startVmps, groupId : " + groupId + "vmpsParam.toString() : " +vmpsParam.toString() + ", time : " + System.currentTimeMillis());
        System.out.println("now in startVmps, groupId : " + groupId + "vmpsParam.toString() : " +vmpsParam.toString() + ", time : " + System.currentTimeMillis());
        StartVmpsRequest startVmpsRequest = new StartVmpsRequest(groupId, vmpsParam);
        confInterfaceService.vmps(startVmpsRequest,"start");
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished startVmps, time : " + System.currentTimeMillis());
        System.out.println("now finished startVmps, time : " + System.currentTimeMillis());
        return startVmpsRequest.getResponseMsg();
    }

    @DeleteMapping(value = "/vmps")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> endVmps(@RequestParam("GroupId") String groupId){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in end vmps, groupId : " + groupId + ", time : " + System.currentTimeMillis());
        System.out.println("now in end vmps, groupId : " + groupId +", time : " + System.currentTimeMillis());
        DeleteVmpsRequest deleteVmpsRequest = new DeleteVmpsRequest(groupId);
        confInterfaceService.endVmps(deleteVmpsRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished end vmps, time : " + System.currentTimeMillis());
        System.out.println("now finished end vmps, time : " + System.currentTimeMillis());
        return deleteVmpsRequest.getResponseMsg();
    }

    @PostMapping(value = "/updateVmps")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> updateVmps(@RequestParam("GroupId") String groupId, @Valid @RequestBody VmpsParam vmpsParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in updateVmps, groupId : " + groupId + ", vmpsParam.toString() : " +vmpsParam.toString() + ", time : " + System.currentTimeMillis());
        System.out.println("now in updateVmps, groupId : " + groupId + "vmpsParam.toString() : " +vmpsParam.toString() + ", time : " + System.currentTimeMillis());
        StartVmpsRequest startVmpsRequest = new StartVmpsRequest(groupId, vmpsParam);
        confInterfaceService.vmps(startVmpsRequest,"update");
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished updateVmps, time : " + System.currentTimeMillis());
        System.out.println("now finished updateVmps, time : " + System.currentTimeMillis());
        return startVmpsRequest.getResponseMsg();
    }

    @GetMapping(value = "/vmps")
    public DeferredResult<ResponseEntity<GetVmpsInfoResponse>> getVmpsInfo(@RequestParam("GroupId") String groupId){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in get vmps info , groupId : " + groupId + ", time : " + System.currentTimeMillis());
        System.out.println("now in get info, groupId : " + groupId +", time : " + System.currentTimeMillis());
        GetVmpsRequest getVmpsRequest = new GetVmpsRequest(groupId);
        confInterfaceService.getVmpsInfo(getVmpsRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished get vmps info, time : " + System.currentTimeMillis());
        System.out.println("now finished get vmps info , time : " + System.currentTimeMillis());
        return getVmpsRequest.getResponseMsg();
    }

    @PostMapping(value = "/mixs")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> startMixs(@RequestParam("GroupId") String groupId,@Valid @RequestBody MixsParam mixsParam){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in start Mixs, groupId : " + groupId + ", mixsParam.toString() : " +mixsParam.toString() +", time : " + System.currentTimeMillis());
        System.out.println("now in start Mixs, groupId : " + groupId + ", mixsParam.toString() : " + mixsParam.toString() + ", time : " + System.currentTimeMillis());
        StartMixRequest startMixRequest = new StartMixRequest(groupId,mixsParam);
        confInterfaceService.startMixs(startMixRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished start Mixs, time : " + System.currentTimeMillis());
        System.out.println("now finished start Mixs, time : " + System.currentTimeMillis());
        return startMixRequest.getResponseMsg();
    }

    @DeleteMapping(value = "/mixs")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> endMixs(@RequestParam("GroupId") String groupId){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in end Mixs, groupId : " + groupId  +", time : " + System.currentTimeMillis());
        System.out.println("now in end Mixs, groupId : " + groupId + ", time : " + System.currentTimeMillis());
        EndMixRequest endMixRequest = new EndMixRequest(groupId);
        confInterfaceService.endMixs(endMixRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished end Mixs, time : " + System.currentTimeMillis());
        System.out.println("now finished end Mixs, time : " + System.currentTimeMillis());
        return endMixRequest.getResponseMsg();
    }

    @GetMapping(value = "/mixs")
    public DeferredResult<ResponseEntity<MixsInfoResponse>> getMixsInfo(@RequestParam("GroupId") String groupId){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in get Mixs info, groupId : " + groupId  +", time : " + System.currentTimeMillis());
        System.out.println("now in get Mixs info, groupId : " + groupId + ", time : " + System.currentTimeMillis());
        GetMixsInfosRequest getMixsInfosRequest = new GetMixsInfosRequest(groupId);
        confInterfaceService.getMixsInfo(getMixsInfosRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished end Mixs, time : " + System.currentTimeMillis());
        System.out.println("now finished get Mixs info, time : " + System.currentTimeMillis());
        return getMixsInfosRequest.getResponseMsg();
    }

    @PostMapping(value = "/mixsMembers")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> addMixsMembers(@RequestParam("GroupId") String groupId,@Valid @RequestBody MixMembers mixMembers){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in add Mixs members , groupId : " + groupId + ", mixMembers.toString() : " +mixMembers.toString() +", time : " + System.currentTimeMillis());
        System.out.println("now in add Mixs members , groupId : " + groupId + ", mixMembers.toString() : " + mixMembers.toString() + ", time : " + System.currentTimeMillis());
        MixMembersRequest mixMembersRequest = new MixMembersRequest(groupId, mixMembers.getMembers());
        confInterfaceService.mixMembers(mixMembersRequest,true);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished add Mixs members, time : " + System.currentTimeMillis());
        System.out.println("now finished add Mixs members, time : " + System.currentTimeMillis());
        return mixMembersRequest.getResponseMsg();
    }

    @DeleteMapping(value = "/mixsMembers")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> deleteMixsMembers(@RequestParam("GroupId") String groupId,@Valid @RequestBody MixMembers mixMembers){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in delete Mixs members , groupId : " + groupId + ", mixMembers.toString() : " +mixMembers.toString() +", time : " + System.currentTimeMillis());
        System.out.println("now in delete Mixs members , groupId : " + groupId + ", mixMembers.toString() : " + mixMembers.toString() + ", time : " + System.currentTimeMillis());
        MixMembersRequest mixMembersRequest = new MixMembersRequest(groupId, mixMembers.getMembers());
        confInterfaceService.mixMembers(mixMembersRequest,false);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished delete Mixs members, time : " + System.currentTimeMillis());
        System.out.println("now finished delete Mixs members, time : " + System.currentTimeMillis());
        return mixMembersRequest.getResponseMsg();
    }

    @PostMapping(value = "/monitors")
    public DeferredResult<ResponseEntity<MonitorsResponse>> startMonitors(@RequestParam("GroupId") String groupId,@Valid @RequestBody MonitorsParams monitorsParams){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in start  Monitors , groupId : " + groupId + ", monitorsParams.toString() : " +monitorsParams.toString() +", time : " + System.currentTimeMillis());
        System.out.println("now in start Monitors , groupId : " + groupId + ", monitorsParams.toString() : " + monitorsParams.toString() + ", time : " + System.currentTimeMillis());
        StartMonitorsRequest startMonitorsRequest = new StartMonitorsRequest(groupId, monitorsParams);
        confInterfaceService.startMonitors(startMonitorsRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now finished start Monitors, time : " + System.currentTimeMillis());
        System.out.println("now finished start Monitors , time : " + System.currentTimeMillis());
        return startMonitorsRequest.getResponseMsg();
    }

    @DeleteMapping(value = "/monitors")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> deleteMonitors(@RequestParam("GroupId") String groupId,@Valid @RequestBody MonitorsParams monitorsParams){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in delete  Monitors , groupId : " + groupId + ", monitorsParams.toString() : " +monitorsParams.toString() +", time : " + System.currentTimeMillis());
        System.out.println("now in delete Monitors , groupId : " + groupId + ", monitorsParams.toString() : " + monitorsParams.toString() + ", time : " + System.currentTimeMillis());
        DeleteMonitorsRequest deleteMonitorsRequest = new DeleteMonitorsRequest(groupId, monitorsParams);
        confInterfaceService.deleteMonitors(deleteMonitorsRequest);
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now delete start Monitors, time : " + System.currentTimeMillis());
        System.out.println("now finished delete Monitors , time : " + System.currentTimeMillis());
        return deleteMonitorsRequest.getResponseMsg();
    }


    @GetMapping(value = "/probe/readiness")
    public ResponseEntity<BaseResponseMsg> probeReadiness(){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in probeReadiness  time : " + System.currentTimeMillis());
        System.out.println("now in probeReadiness  time : " + System.currentTimeMillis());
        BaseResponseMsg baseResponseMsg = new BaseResponseMsg(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        return new ResponseEntity<>(baseResponseMsg, HttpStatus.OK);
    }

    @GetMapping(value = "/probe/liveness")
    public ResponseEntity<BaseResponseMsg> probeLiveness(){
        if(ConfInterfaceInitializingService.probe){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in probeLiveness success time : " + System.currentTimeMillis());
            System.out.println("now in probeLiveness success time : " + System.currentTimeMillis());
            BaseResponseMsg baseResponseMsg = new BaseResponseMsg(ConfInterfaceResult.PROBE_SUCCESS.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.PROBE_SUCCESS.getMessage());
            return new ResponseEntity<>(baseResponseMsg, HttpStatus.OK);
        }
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"now in probeLiveness failed time : " + System.currentTimeMillis());
        System.out.println("now in probeLiveness failed time : " + System.currentTimeMillis());
        BaseResponseMsg baseResponseMsg = new BaseResponseMsg(ConfInterfaceResult.PROBE_FAILED.getCode(), HttpStatus.INTERNAL_SERVER_ERROR.value(), ConfInterfaceResult.PROBE_FAILED.getMessage());
        return new ResponseEntity<>(baseResponseMsg, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<BaseResponseMsg> handleArgumentNotValidException(MethodArgumentNotValidException exception) {
        BaseResponseMsg errorResponse = new BaseResponseMsg();
        errorResponse.setCode(ConfInterfaceResult.INVALID_PARAM.getCode());
        errorResponse.setMessage(exception.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
