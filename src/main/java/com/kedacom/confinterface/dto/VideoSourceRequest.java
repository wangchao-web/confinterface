package com.kedacom.confinterface.dto;


import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class VideoSourceRequest extends BaseRequestMsg<BaseResponseMsg> {

    public VideoSourceRequest(String groupId, VideoSourceParam videoSourceParam) {
        super(groupId);
        this.videoSourceParam = videoSourceParam;
    }

    public VideoSourceParam getVideoSourceParam() {
        return videoSourceParam;
    }

    public void setVideoSourceParam(VideoSourceParam videoSourceParam) {
        this.videoSourceParam = videoSourceParam;
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        BaseResponseMsg baseResponseMsg = new BaseResponseMsg(code, status.value(), message);
        ResponseEntity<BaseResponseMsg> responseEntity = new ResponseEntity<>(baseResponseMsg, status);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        BaseResponseMsg baseResponseMsg = new BaseResponseMsg(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        ResponseEntity<BaseResponseMsg> responseEntity = new ResponseEntity<>(baseResponseMsg, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

   private VideoSourceParam videoSourceParam;
}
