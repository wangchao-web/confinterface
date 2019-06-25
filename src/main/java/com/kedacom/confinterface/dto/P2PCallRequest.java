package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class P2PCallRequest extends BaseRequestMsg<P2PCallResponse> {
    public P2PCallRequest(String groupId, String account){
        super(groupId);
        this.account = account;
    }

    public void addForwardResource(MediaResource mediaResource) {
        if (null == forwardResources)
            forwardResources = new ArrayList<>();

        forwardResources.add(mediaResource);
    }

    public void addReverseResource(MediaResource mediaResource){
        if (null == reverseResources)
            reverseResources = new ArrayList<>();

        reverseResources.add(mediaResource);
    }

    @Override
    public void removeMsg(String msg) {
        super.removeMsg(msg);
        if (waitMsg.isEmpty()){
            System.out.println("移除成功");
            makeSuccessResponseMsg();
        }
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        P2PCallResponse p2PCallResponse = new P2PCallResponse(code, status.value(), message);
        ResponseEntity<P2PCallResponse> responseEntity = new ResponseEntity<>(p2PCallResponse, status);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        P2PCallResponse p2PCallResponse = new P2PCallResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        p2PCallResponse.setAccount(account);
        p2PCallResponse.setForwardResources(forwardResources);
        p2PCallResponse.setReverseResources(reverseResources);
        ResponseEntity<P2PCallResponse> responseEntity = new ResponseEntity<>(p2PCallResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    private String account;
    private List<MediaResource> forwardResources;
    private List<MediaResource> reverseResources;
}
