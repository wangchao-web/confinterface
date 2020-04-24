package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class StartMixRequest extends BaseRequestMsg<BaseResponseMsg> {
    public StartMixRequest(String groupId) {
        super(groupId);
    }

    public StartMixRequest(String groupId, MixsParam mixsParam) {
        super(groupId);
        this.mixsParam = mixsParam;
    }

    public MixsParam getMixsParam() {
        return mixsParam;
    }

    public void setMixsParam(MixsParam mixsParam) {
        this.mixsParam = mixsParam;
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

    private MixsParam mixsParam;
}
