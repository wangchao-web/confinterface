package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SendIFrameRequest extends BaseRequestMsg<SendIFrameResponse>{

    public SendIFrameRequest(String groupId, SendIFrameParam sendIFrameParam) {
        super(groupId);
        this.sendIFrameParam = sendIFrameParam;
    }

    public SendIFrameParam getSendIFrameParam() {
        return sendIFrameParam;
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        SendIFrameResponse sendIFrameRsp = new SendIFrameResponse(code, status.value(), message);
        sendIFrameRsp.setSendIFrameParam(sendIFrameParam);
        ResponseEntity<SendIFrameResponse> responseEntity = new ResponseEntity<>(sendIFrameRsp, status);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        SendIFrameResponse sendIFrameRsp = new SendIFrameResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        sendIFrameRsp.setSendIFrameParam(sendIFrameParam);
        ResponseEntity<SendIFrameResponse> responseEntity = new ResponseEntity<>(sendIFrameRsp, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    private SendIFrameParam sendIFrameParam;
}
