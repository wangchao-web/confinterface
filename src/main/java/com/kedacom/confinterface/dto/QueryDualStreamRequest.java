package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class QueryDualStreamRequest extends BaseRequestMsg<QueryDualStreamResponse> {
    public QueryDualStreamRequest(String groupId){
        super(groupId);
        resources = null;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void addResource(MediaResource mediaResource) {
        if (null == resources)
            resources = new ArrayList<>();

        resources.add(mediaResource);
    }

    @Override
    public void makeSuccessResponseMsg() {
        QueryDualStreamResponse queryDualStreamResponse = new QueryDualStreamResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        queryDualStreamResponse.setType(type);
        queryDualStreamResponse.setResources(resources);
        ResponseEntity<QueryDualStreamResponse> responseEntity = new ResponseEntity<>(queryDualStreamResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        QueryDualStreamResponse queryDualStreamResponse = new QueryDualStreamResponse(code, status.value(), message);
        ResponseEntity<QueryDualStreamResponse> responseEntity = new ResponseEntity<>(queryDualStreamResponse, status);
        responseMsg.setResult(responseEntity);
    }

    private int type;     //1-终端，2-监控前端
    private List<MediaResource> resources;
}
