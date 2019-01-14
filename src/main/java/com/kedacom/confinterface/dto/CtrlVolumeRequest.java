package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CtrlVolumeRequest extends BaseRequestMsg<BaseResponseMsg> {

    public CtrlVolumeRequest(String groupId, String mtE164, CtrlVolumeParam ctrlVolumeParam) {
        super(groupId);
        this.mtE164 = mtE164;
        this.ctrlVolumeParam = ctrlVolumeParam;
    }

    public String getMtE164() {
        return mtE164;
    }

    public CtrlVolumeParam getCtrlVolumeParam() {
        return ctrlVolumeParam;
    }

    public void setCtrlVolumeParam(CtrlVolumeParam ctrlVolumeParam) {
        this.ctrlVolumeParam = ctrlVolumeParam;
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

    private String mtE164;
    private CtrlVolumeParam ctrlVolumeParam;
}
