package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SendSmsRequest extends BaseRequestMsg<BaseResponseMsg> {

    public SendSmsRequest(String groupId) {
        super(groupId);
    }

    public SendSmsRequest(String groupId, SendSmsParam sendSmsParam) {
        super(groupId);
        this.sendSmsParam = sendSmsParam;
    }

    public SendSmsParam getSendSmsParam() {
        return sendSmsParam;
    }

    public void setSendSmsParam(SendSmsParam sendSmsParam) {
        this.sendSmsParam = sendSmsParam;
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

    private SendSmsParam sendSmsParam;
}
