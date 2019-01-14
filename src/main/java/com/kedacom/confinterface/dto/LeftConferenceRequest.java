package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class LeftConferenceRequest extends BaseRequestMsg<BaseResponseMsg> {
    public LeftConferenceRequest(String groupId, LeftConferenceParam leftConferenceParam) {
        super(groupId);
        this.leftConferenceParam = leftConferenceParam;
    }

    public LeftConferenceParam getLeftConferenceParam() {
        return leftConferenceParam;
    }

    @Override
    public void makeSuccessResponseMsg() {
        BaseResponseMsg leftConfResponse = new BaseResponseMsg(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        ResponseEntity<BaseResponseMsg> responseEntity = new ResponseEntity<>(leftConfResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        BaseResponseMsg leftConfResponse = new BaseResponseMsg(code, status.value(), message);
        ResponseEntity<BaseResponseMsg> responseEntity = new ResponseEntity<>(leftConfResponse, status);
        responseMsg.setResult(responseEntity);
    }

    private LeftConferenceParam leftConferenceParam;
}
