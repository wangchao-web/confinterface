package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class QueryConfsCascadesMtsRequest extends BaseRequestMsg<QueryConfsCascadesMtsResponse>{

    public QueryConfsCascadesMtsRequest(String groupId ,String cascadeId){
        super(groupId);
        this.cascadeId = cascadeId;
        mts = new ArrayList<>();
    }

    public void addConfsCascadesMtsInfo(ConfsCascadesMtsInfo confsCascadesMtsInfo) {
        mts.add(confsCascadesMtsInfo);
    }

    public List<ConfsCascadesMtsInfo> getMts() {
        return mts;
    }

    public void setMts(List<ConfsCascadesMtsInfo> mts) {
        this.mts = mts;
    }

    public String getCascadeId() {
        return cascadeId;
    }

    public void setCascadeId(String cascadeId) {
        this.cascadeId = cascadeId;
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        QueryConfsCascadesMtsResponse queryConfsCascadesMtsResponse = new QueryConfsCascadesMtsResponse(code, status.value(), message);
        queryConfsCascadesMtsResponse.setMts(null);
        ResponseEntity<QueryConfsCascadesMtsResponse> responseResponseEntity = new ResponseEntity<>(queryConfsCascadesMtsResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        QueryConfsCascadesMtsResponse queryConfsCascadesMtsResponse = new QueryConfsCascadesMtsResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        queryConfsCascadesMtsResponse.setMts(mts);
        ResponseEntity<QueryConfsCascadesMtsResponse> responseResponseEntity = new ResponseEntity<>(queryConfsCascadesMtsResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    private List<ConfsCascadesMtsInfo> mts;
    private String cascadeId;
}
