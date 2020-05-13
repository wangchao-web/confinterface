package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DeleteMonitorsRequest extends BaseRequestMsg<BaseResponseMsg>{
    public DeleteMonitorsRequest(String groupId, MonitorsParams monitorsParams) {
        super(groupId);
        this.monitorsParams = monitorsParams;
    }

    public MonitorsParams getMonitorsParams() {
        return monitorsParams;
    }

    public void setMonitorsParams(MonitorsParams monitorsParams) {
        this.monitorsParams = monitorsParams;
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

    private MonitorsParams monitorsParams;
}
