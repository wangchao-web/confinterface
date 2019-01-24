package com.kedacom.confinterface.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class StartDualStreamRequest extends BaseRequestMsg<StartDualResponse> {
    public StartDualStreamRequest(String groupId, DualStreamParam dualStreamParam){
        super(groupId);
        this.dualStreamParam = dualStreamParam;
    }

    public DualStreamParam getDualStreamParam() {
        return dualStreamParam;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public void makeSuccessResponseMsg() {
        StartDualResponse startDualResponse = new StartDualResponse(0, HttpStatus.OK.value(), "Ok");
        startDualResponse.setResourceId(resourceId);
        ResponseEntity<StartDualResponse> responseEntity = new ResponseEntity<>(startDualResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        StartDualResponse startDualResponse = new StartDualResponse(code, status.value(), message);
        startDualResponse.setResourceId(null);
        ResponseEntity<StartDualResponse> responseEntity = new ResponseEntity<>(startDualResponse, status);
        responseMsg.setResult(responseEntity);
    }

    private DualStreamParam dualStreamParam;
    private String resourceId;
}
