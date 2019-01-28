package com.kedacom.confinterface.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class StartDualStreamRequest extends BaseRequestMsg<StartDualResponse> {
    public StartDualStreamRequest(String groupId, DualStreamParam dualStreamParam){
        super(groupId);
        this.dualStreamParam = dualStreamParam;
    }

    public DualStreamParam getDualStreamParam() {
        return dualStreamParam;
    }

    public void addResource(MediaResource resource){
        if (null == resources)
            resources = new ArrayList<>();

        resources.add(resource);
    }

    @Override
    public void makeSuccessResponseMsg() {
        StartDualResponse startDualResponse = new StartDualResponse(0, HttpStatus.OK.value(), "Ok");
        startDualResponse.setResources(resources);
        ResponseEntity<StartDualResponse> responseEntity = new ResponseEntity<>(startDualResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        StartDualResponse startDualResponse = new StartDualResponse(code, status.value(), message);
        startDualResponse.setResources(null);
        ResponseEntity<StartDualResponse> responseEntity = new ResponseEntity<>(startDualResponse, status);
        responseMsg.setResult(responseEntity);
    }

    private DualStreamParam dualStreamParam;
    private List<MediaResource> resources;
}
