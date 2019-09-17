package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class JoinConferenceRequest extends BaseRequestMsg<BaseResponseMsg> {

    public JoinConferenceRequest(String groupId, List<Terminal> mts,int confinterface) {
        super(groupId);
        this.mts = mts;
        if(confinterface == 1){
            this.confinterface = true;
        }else{
            this.confinterface = false;
        }

    }

    public JoinConferenceRequest(String groupId, List<Terminal> mts) {
        super(groupId);
        this.mts = mts;
    }

    public void setMts(List<Terminal> mts) {
        this.mts = mts;
    }

    public List<Terminal> getMts() {
        return mts;
    }

    public boolean isConfinterface() {
        return confinterface;
    }

    public void setConfinterface(boolean confinterface) {
        this.confinterface = confinterface;
    }

    @Override
    public void makeSuccessResponseMsg() {
        BaseResponseMsg baseResponseMsg = new BaseResponseMsg(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        ResponseEntity<BaseResponseMsg> responseEntity = new ResponseEntity<>(baseResponseMsg, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        BaseResponseMsg baseResponseMsg = new BaseResponseMsg(code, status.value(), message);
        ResponseEntity<BaseResponseMsg> responseEntity = new ResponseEntity<>(baseResponseMsg, status);
        responseMsg.setResult(responseEntity);
    }

    private List<Terminal> mts;

    private boolean confinterface = false;
}
