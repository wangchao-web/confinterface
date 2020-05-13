package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CancelInspectionRequest extends BaseRequestMsg<BaseResponseMsg> {

    public CancelInspectionRequest(String groupId, InspectionParam inspectionParam) {
        super(groupId);
        this.inspectionParam = inspectionParam;
    }

    public InspectionParam getInspectionParam() {
        return inspectionParam;
    }

    public void setInspectionParam(InspectionParam inspectionParam) {
        this.inspectionParam = inspectionParam;
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

    private InspectionParam inspectionParam;
}
