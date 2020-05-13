package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class QueryConfInfoRequest extends BaseRequestMsg<QueryConfInfoResponse> {
    public QueryConfInfoRequest(String groupId){
        super(groupId);
    }

    public void addConfInfo(ConfInfo confInfo) {
        this.confInfo = confInfo ;
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        QueryConfInfoResponse queryConfInfoResponse = new QueryConfInfoResponse(code, status.value(), message);
        queryConfInfoResponse.setConfInfo(null);
        ResponseEntity<QueryConfInfoResponse> responseResponseEntity = new ResponseEntity<>(queryConfInfoResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        QueryConfInfoResponse queryConfInfoResponse = new QueryConfInfoResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        queryConfInfoResponse.setConfInfo(confInfo);
        ResponseEntity<QueryConfInfoResponse> responseResponseEntity = new ResponseEntity<>(queryConfInfoResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    private ConfInfo confInfo;
}
