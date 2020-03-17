package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class GetConfMtRequest extends BaseRequestMsg<QueryConfMtInfoResponse> {
    public GetConfMtRequest(String groupId,String mtE164){
        super(groupId);
        this.mtE164 = mtE164;
    }

    public ConfMtInfo getConfMtInfo() {
        return confMtInfo;
    }

    public void setConfMtInfo(ConfMtInfo confMtInfo) {
        this.confMtInfo = confMtInfo;
    }

    public String getMtE164() {
        return mtE164;
    }

    public void setMtE164(String mtE164) {
        this.mtE164 = mtE164;
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        QueryConfMtInfoResponse queryConfMtInfoResponse = new QueryConfMtInfoResponse(code, status.value(), message);
        queryConfMtInfoResponse.setConfMtInfo(null);
        ResponseEntity<QueryConfMtInfoResponse> responseResponseEntity = new ResponseEntity<>(queryConfMtInfoResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        QueryConfMtInfoResponse queryConfMtInfoResponse = new QueryConfMtInfoResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        queryConfMtInfoResponse.setConfMtInfo(confMtInfo);
        ResponseEntity<QueryConfMtInfoResponse> responseResponseEntity = new ResponseEntity<>(queryConfMtInfoResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    private ConfMtInfo confMtInfo;
    private String mtE164;
}
