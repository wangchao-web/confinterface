package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class MixMembersRequest extends BaseRequestMsg<BaseResponseMsg> {

    public MixMembersRequest(String groupId, List<Terminal> mixMembers) {
        super(groupId);
        this.mixMembers = mixMembers;
    }

    public MixMembersRequest(String groupId) {
        super(groupId);
    }

    public List<Terminal> getMixMembers() {
        return mixMembers;
    }

    public void setMixMembers(List<Terminal> mixMembers) {
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

    private List<Terminal> mixMembers;

}
