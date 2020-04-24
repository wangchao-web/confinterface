package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class SendSmsRequest extends BaseRequestMsg<SendSmsResponse> {

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

    public void addMtE164(Terminal Terminal){
        mts.add(Terminal);
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        SendSmsResponse sendSmsResponse = new SendSmsResponse(code, status.value(), message);
        sendSmsResponse.setMtE164(null);
        ResponseEntity<SendSmsResponse> responseEntity = new ResponseEntity<>(sendSmsResponse, status);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        SendSmsResponse sendSmsResponse = new SendSmsResponse(0, HttpStatus.OK.value(), "Ok");
        sendSmsResponse.setMtE164(mts);
        ResponseEntity<SendSmsResponse> responseEntity = new ResponseEntity<>(sendSmsResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    private SendSmsParam sendSmsParam;
    private List<Terminal> mts = new ArrayList<>();
}
