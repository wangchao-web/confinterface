package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class StartVmpsRequest extends BaseRequestMsg<BaseResponseMsg> {
    public StartVmpsRequest(String groupId, VmpsParam vmpsParam) {
        super(groupId);
        this.vmpsParam = vmpsParam;
    }

    public StartVmpsRequest(String groupId) {
        super(groupId);
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        BaseResponseMsg baseResponseMsg = new BaseResponseMsg(code, status.value(), message);
        ResponseEntity<BaseResponseMsg> responseEntity = new ResponseEntity<>(baseResponseMsg, status);
        responseMsg.setResult(responseEntity);
    }

    public VmpsParam getVmpsParam() {
        return vmpsParam;
    }

    public void setVmpsParam(VmpsParam vmpsParam) {
        this.vmpsParam = vmpsParam;
    }

    @Override
    public void makeSuccessResponseMsg() {
        BaseResponseMsg baseResponseMsg = new BaseResponseMsg(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        ResponseEntity<BaseResponseMsg> responseEntity = new ResponseEntity<>(baseResponseMsg, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    private VmpsParam vmpsParam;
}
