package com.kedacom.confinterface.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class LeftDiscussionGroupRequest extends BaseRequestMsg<LeftDiscussionGroupResponse> {

    public LeftDiscussionGroupRequest(String groupId, List<LeftDiscussionGroupMt> mts) {
        super(groupId);
        this.mts = mts;
    }

    public List<LeftDiscussionGroupMt> getMts() {
        return mts;
    }

    public void setMts(List<LeftDiscussionGroupMt> mts) {
        this.mts = mts;
    }

    @Override
    public void removeMsg(String msg) {
        super.removeMsg(msg);

        if (waitMsg.isEmpty()) {
            makeSuccessResponseMsg();
        }
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        LeftDiscussionGroupResponse leftDiscussionGroupResponse = new LeftDiscussionGroupResponse(code, status.value(), message);
        leftDiscussionGroupResponse.setMts(mts);
        ResponseEntity<LeftDiscussionGroupResponse> responseEntity = new ResponseEntity<>(leftDiscussionGroupResponse, status);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        LeftDiscussionGroupResponse leftDiscussionGroupResponse = new LeftDiscussionGroupResponse(0, HttpStatus.OK.value(), "Ok");
        leftDiscussionGroupResponse.setMts(mts);
        ResponseEntity<LeftDiscussionGroupResponse> responseEntity = new ResponseEntity<>(leftDiscussionGroupResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    //保存退出讨论组失败的终端信息
    private List<LeftDiscussionGroupMt> mts;
}
