package com.kedacom.confinterface.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class BroadCastRequest extends BaseRequestMsg<BroadCastResponse> {

    public BroadCastRequest(String groupId, BroadCastParam broadCastParam) {
        super(groupId);
        this.broadCastParam = broadCastParam;
    }

    public BroadCastParam getBroadCastParam() {
        return broadCastParam;
    }

    public void setBroadCastParam(BroadCastParam broadCastParam) {
        this.broadCastParam = broadCastParam;
    }

    public void setReverseResources(List<MediaResource> reverseResources) {
        this.reverseResources = reverseResources;
    }

    public void setForwardResources(List<MediaResource> forwardResources) {
        this.forwardResources = forwardResources;
    }

    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        BroadCastResponse broadCastResponse = new BroadCastResponse(code, status.value(), message);
        ResponseEntity<BroadCastResponse> responseEntity = new ResponseEntity<>(broadCastResponse, status);
        responseMsg.setResult(responseEntity);
    }

    public void makeSuccessResponseMsg() {
        BroadCastResponse broadCastResponse = new BroadCastResponse(0, HttpStatus.OK.value(), "ok");
        broadCastResponse.setType(broadCastParam.getType());
        broadCastResponse.setMtE164(broadCastParam.getMtE164());
        broadCastResponse.setForwardResources(forwardResources);
        broadCastResponse.setReverseResources(reverseResources);
        ResponseEntity<BroadCastResponse> responseEntity = new ResponseEntity<>(broadCastResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    private BroadCastParam broadCastParam;
    private List<MediaResource> forwardResources;
    private List<MediaResource> reverseResources;
}
