package com.kedacom.confinterface.controller;

import com.kedacom.confinterface.dto.*;
import com.kedacom.confinterface.inner.SubscribeMsgTypeEnum;
import com.kedacom.confinterface.service.ConfInterfacePublishService;
import com.kedacom.confinterface.service.ConfInterfaceService;
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
        System.out.println("now in joinConference, groupId : " + groupId + ", time : " + System.currentTimeMillis());
        JoinConferenceRequest joinConferenceRequest = new JoinConferenceRequest(groupId, joinConferenceParam.getMts());
        confInterfaceService.joinConference(joinConferenceRequest);
        System.out.println("now finished joinConference, time : " + System.currentTimeMillis());
        return joinConferenceRequest.getResponseMsg();
    }

    @DeleteMapping(value = "/participants")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> leftConference(@RequestParam("GroupId") String groupId, @Valid @RequestBody LeftConferenceParam leftConferenceParam) {
        System.out.println("now in leftConference, groupId: " + groupId + ", cancelGroup: " + leftConferenceParam.isCancelGroup() + ", time : " + System.currentTimeMillis());
        LeftConferenceRequest leftConferenceRequest = new LeftConferenceRequest(groupId, leftConferenceParam);
        confInterfaceService.leftConference(leftConferenceRequest);
        System.out.println("now finished leftConference, time : " + System.currentTimeMillis());
        return leftConferenceRequest.getResponseMsg();
    }

    @PostMapping(value = "/broadcast")
    public DeferredResult<ResponseEntity<BroadCastResponse>> setBroadcastSrc(@RequestParam("GroupId") String groupId, @Valid @RequestBody BroadCastParam broadCastParam) {
        System.out.println("now in setBroadcastSrc, groupId: " + groupId + ", broadCastParam:" + broadCastParam + ", time : " + System.currentTimeMillis());
        BroadCastRequest broadCastRequest = new BroadCastRequest(groupId, broadCastParam);
        confInterfaceService.setBroadcastSrc(broadCastRequest);
        System.out.println("now finished setBroadcastSrc, time : " + System.currentTimeMillis());
        return broadCastRequest.getResponseMsg();
    }

    @DeleteMapping(value = "/broadcast")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> delBroadcastSrc(@RequestParam("GroupId") String groupId) {
        System.out.println("now in delBroadcastSrc, groupId: " + groupId + ", time : " + System.currentTimeMillis());
        CancelBroadCastRequest cancelBroadCastRequest = new CancelBroadCastRequest(groupId);
        confInterfaceService.cancelBroadcast(cancelBroadCastRequest);
        System.out.println("now finished delBroadcastSrc, time : " + System.currentTimeMillis());
        return cancelBroadCastRequest.getResponseMsg();
    }

    @PostMapping(value = "/discussiongroup")
    public DeferredResult<ResponseEntity<JoinDisscussionGroupResponse>> joinDiscussionGroup(@RequestParam("GroupId") String groupId, @Valid @RequestBody JoinDisscussionGroupParam joinDisscussionGroupParam) {
        System.out.println("now in joinDiscussionGroup, groupId: " + groupId + ", time : " + System.currentTimeMillis());
        JoinDiscussionGroupRequest joinDiscussionGroupRequest = new JoinDiscussionGroupRequest(groupId, joinDisscussionGroupParam.getMts());
        confInterfaceService.joinDiscussionGroup(joinDiscussionGroupRequest);
        System.out.println("now finished joinDiscussionGroup, time : " + System.currentTimeMillis());
        return joinDiscussionGroupRequest.getResponseMsg();
    }

    @DeleteMapping(value = "/discussiongroup")
    public DeferredResult<ResponseEntity<LeftDiscussionGroupResponse>> leftDiscussionGroup(@RequestParam("GroupId") String groupId, @Valid @RequestBody LeftDiscussionGroupParam leftDisscussionGroupParam) {
        System.out.println("now in leftDiscussionGroup, groupId: " + groupId + ", time : " + System.currentTimeMillis());
        LeftDiscussionGroupRequest leftDiscussionGroupRequest = new LeftDiscussionGroupRequest(groupId, leftDisscussionGroupParam.getMts());
        confInterfaceService.leftDiscussionGroup(leftDiscussionGroupRequest);
        System.out.println("now finished leftDiscussionGroup, time : " + System.currentTimeMillis());
        return leftDiscussionGroupRequest.getResponseMsg();
    }

    @PostMapping(value = "/inspections")
    public DeferredResult<ResponseEntity<InspectionResponse>> inspection(@RequestParam("GroupId") String groupId, @Valid @RequestBody InspectionParam inspectionParam) {
        System.out.println("now in inspection, groupId: " + groupId +", InspectionParam:"+ inspectionParam + ", time : " + System.currentTimeMillis());
        InspectionRequest inspectionRequest = new InspectionRequest(groupId, inspectionParam);
        confInterfaceService.startInspection(inspectionRequest);
        System.out.println("now finished inspection, time : " + System.currentTimeMillis());
        return inspectionRequest.getResponseMsg();
    }

    @DeleteMapping(value = "/inspections")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> cancelInspection(@RequestParam("GroupId") String groupId, @Valid @RequestBody InspectionParam inspectionParam) {
        System.out.println("now in cancelInspection, groupId: " + groupId + ", cancelInspectionParam:" + inspectionParam + ", time : " + System.currentTimeMillis());
        CancelInspectionRequest cancelInspectionRequest = new CancelInspectionRequest(groupId, inspectionParam);
        confInterfaceService.cancelInspection(cancelInspectionRequest);
        System.out.println("now finished cancelInspection, time : " + System.currentTimeMillis());
        return cancelInspectionRequest.getResponseMsg();
    }

    @PostMapping(value = "/mts/{mtE164}/camera")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> ctrlCamera(@RequestParam("GroupId") String groupId, @PathVariable String mtE164, @Valid @RequestBody CameraCtrlParam cameraCtrlParam) {
        System.out.println("now in ctrlCamera, groupId: " + groupId + ", mtE164:" + mtE164 + ", CameraCtrlParam:" + cameraCtrlParam + ", time : " + System.currentTimeMillis());
        CameraCtrlRequest cameraCtrlRequest = new CameraCtrlRequest(groupId, mtE164, cameraCtrlParam);
        confInterfaceService.ctrlCamera(cameraCtrlRequest);
        System.out.println("now finished ctrlCamera, time : " + System.currentTimeMillis());
        return cameraCtrlRequest.getResponseMsg();
    }

    @PostMapping(value = "/subscription/terminalstatus")
    public ResponseEntity<BaseResponseMsg> subscribeTerminalStatus(@RequestParam("GroupId") String groupId, @Valid @RequestBody SubscribeTerminalStatusParam subscribeTerminalStatusParam){
        System.out.println("now in subscribeTerminalStatus, groupId:"+groupId+", subscribeParam:"+subscribeTerminalStatusParam);
        confInterfacePublishService.addSubscribeMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS.getType(), groupId, subscribeTerminalStatusParam.getUrl());
        BaseResponseMsg baseResponseMsg = new BaseResponseMsg(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        return new ResponseEntity<>(baseResponseMsg, HttpStatus.OK);
    }

    @PostMapping(value = "/iframe")
    public DeferredResult<ResponseEntity<SendIFrameResponse>> sendIFrame(@RequestParam("GroupId") String groupId, @Valid @RequestBody SendIFrameParam sendIFrameParam){
        System.out.println("now in sendIFrame, groupId:"+groupId+", iframeParam:"+sendIFrameParam);
        SendIFrameRequest sendIFrameRequest = new SendIFrameRequest(groupId, sendIFrameParam);
        confInterfaceService.sendIFrame(sendIFrameRequest);
        System.out.println("now finished sendIFrame, time:"+System.currentTimeMillis());
        return sendIFrameRequest.getResponseMsg();
    }

    @PostMapping(value = "/mts/{mtE164}/volume")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> ctrlVolume(@RequestParam("GroupId") String groupId, @PathVariable String mtE164, @Valid @RequestBody CtrlVolumeParam ctrlVolumeParam){
        System.out.println("now in ctrlVolume, groupId:"+groupId + ", mtE164:" + mtE164 +", ctrlVolumeParam:"+ctrlVolumeParam);
        CtrlVolumeRequest ctrlVolumeRequest = new CtrlVolumeRequest(groupId, mtE164, ctrlVolumeParam);
        confInterfaceService.ctrlVolume(ctrlVolumeRequest);
        System.out.println("now finished ctrlVolume, time:"+System.currentTimeMillis());
        return ctrlVolumeRequest.getResponseMsg();
    }

    @PostMapping(value = "/mts/{mtE164}/silence")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> silence(@RequestParam("GroupId") String groupId, @PathVariable String mtE164, @Valid @RequestBody SilenceOrMuteParam silenceParam){
        System.out.println("now in setSilence, groupId:"+groupId + ", mtE164:" + mtE164 +", silenceParam:"+silenceParam);
        return silenceOrMute(groupId, mtE164, true, silenceParam);
    }

    @PostMapping(value = "/mts/{mtE164}/mute")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> mute(@RequestParam("GroupId") String groupId, @PathVariable String mtE164, @Valid @RequestBody SilenceOrMuteParam muteParam){
        System.out.println("now in setMute, groupId:"+groupId + ", mtE164:" + mtE164 +", muteParam:"+muteParam);
        return silenceOrMute(groupId, mtE164, false, muteParam);
    }

    @PostMapping(value = "/dualStream")
    public DeferredResult<ResponseEntity<StartDualResponse>> startDual(@RequestParam("GroupId") String groupId, @Valid @RequestBody DualStreamParam dualStreamParam){
        System.out.println("now in startDual, groupId:"+groupId + ", dualStreamParam:" + dualStreamParam);
        StartDualStreamRequest startDualStreamRequest = new StartDualStreamRequest(groupId, dualStreamParam);
        confInterfaceService.ctrlDualStream(startDualStreamRequest, dualStreamParam.getMtE164(), true);
        return startDualStreamRequest.getResponseMsg();
    }

    @DeleteMapping(value = "/dualStream")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> stopDual(@RequestParam("GroupId") String groupId, @Valid @RequestBody DualStreamParam dualStreamParam){
        System.out.println("now in stopDual, groupId:"+groupId + ", dualStreamParam:" + dualStreamParam);
        CancelDualStreamRequest cancelDualStreamRequest = new CancelDualStreamRequest(groupId, dualStreamParam);
        confInterfaceService.ctrlDualStream(cancelDualStreamRequest, dualStreamParam.getMtE164(), false);
        return cancelDualStreamRequest.getResponseMsg();
    }

    @GetMapping(value = "/dualStream")
    public DeferredResult<ResponseEntity<QueryDualStreamResponse>> queryDualStream(@RequestParam("GroupId") String groupId){
        System.out.println("now in queryDualStream, groupId:"+groupId);
        QueryDualStreamRequest queryDualStreamRequest = new QueryDualStreamRequest(groupId);
        confInterfaceService.queryDualStream(queryDualStreamRequest);
        return queryDualStreamRequest.getResponseMsg();
    }

    @GetMapping(value = "/vmts")
    public DeferredResult<ResponseEntity<QueryVmtsResponse>> getVmts(){
        System.out.println("query all vmts! time:"+System.currentTimeMillis());
        QueryVmtsRequest queryVmtsRequest = new QueryVmtsRequest();
        confInterfaceService.queryVmts(queryVmtsRequest);
        System.out.println("now finished query all vmts, time:"+System.currentTimeMillis());
        return queryVmtsRequest.getResponseMsg();
    }
	 @PostMapping(value = "p2pcall")
    public DeferredResult<ResponseEntity<P2PCallResponse>> p2pCall(@RequestParam("GroupId") String groupId, @Valid @RequestBody P2PCallParam p2PCallParam){
        System.out.println("now in p2pCall, groupId:"+groupId+", p2pCallParam:"+p2PCallParam);
        P2PCallRequest p2PCallRequest = new P2PCallRequest(groupId, p2PCallParam.getAccount());
        confInterfaceService.p2pCall(p2PCallRequest, p2PCallParam);
        return p2PCallRequest.getResponseMsg();
    }

    @DeleteMapping(value = "p2pcall")
    public DeferredResult<ResponseEntity<BaseResponseMsg>> cancelP2PCall(@RequestParam("GroupId") String groupId, @Valid @RequestBody CancelP2PCallParam cancelP2PCallParam){
        System.out.println("now in p2pCall, groupId:"+groupId+", p2pCallParam:"+cancelP2PCallParam);
        CancelP2PCallRequest cancelP2PCallRequest = new CancelP2PCallRequest(groupId);
        confInterfaceService.cancelP2PCall(cancelP2PCallRequest, cancelP2PCallParam);
        return cancelP2PCallRequest.getResponseMsg();
    }

    private DeferredResult<ResponseEntity<BaseResponseMsg>> silenceOrMute(String groupId, String mtE164, boolean silence, SilenceOrMuteParam silenceOrMuteParam){
        CtrlSilenceOrMuteRequest ctrlSilenceOrMuteRequest = new CtrlSilenceOrMuteRequest(groupId, mtE164, silence, silenceOrMuteParam);
        confInterfaceService.silenceOrMute(ctrlSilenceOrMuteRequest);
        return ctrlSilenceOrMuteRequest.getResponseMsg();
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
