package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetMixsInfosRequest extends BaseRequestMsg<MixsInfoResponse> {
    public GetMixsInfosRequest(String groupId) {
        super(groupId);
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public List<Terminal> getMembers() {
        return members;
    }

    public void setMembers(List<Terminal> members) {
        this.members = members;
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        MixsInfoResponse mixsInfoResponse = new MixsInfoResponse(code, status.value(), message);
        ResponseEntity<MixsInfoResponse> responseResponseEntity = new ResponseEntity<>(mixsInfoResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        MixsInfoResponse mixsInfoResponse = new MixsInfoResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        mixsInfoResponse.setMode(mode);
        mixsInfoResponse.setMembers(members);
        ResponseEntity<MixsInfoResponse> responseResponseEntity = new ResponseEntity<>(mixsInfoResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    private int mode;
    private List<Terminal> members; //混音成员数组
}
