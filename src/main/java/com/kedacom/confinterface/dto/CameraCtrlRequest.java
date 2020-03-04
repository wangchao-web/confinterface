package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CameraCtrlRequest extends BaseRequestMsg<BaseResponseMsg> {

    public CameraCtrlRequest(String groupId, CameraCtrlParam cameraCtrlParam) {
        super(groupId);
        this.cameraCtrlParam = cameraCtrlParam;
    }

    public CameraCtrlParam getCameraCtrlParam() {
        return cameraCtrlParam;
    }

    public void setCameraCtrlParam(CameraCtrlParam cameraCtrlParam) {
        this.cameraCtrlParam = cameraCtrlParam;
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

    private CameraCtrlParam cameraCtrlParam;
}
