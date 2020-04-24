package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class MixMembersRequest extends BaseRequestMsg<BaseResponseMsg> {

    public MixMembersRequest(String groupId, MixMembers mixMembers) {
        super(groupId);
        this.mixMembers = mixMembers;
    }

    public MixMembersRequest(String groupId) {
        super(groupId);
    }

    public MixMembers getMixMembers() {
        return mixMembers;
    }

    public void setMixMembers(MixMembers mixMembers) {
        this.mixMembers = mixMembers;
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

    private MixMembers mixMembers;
}
