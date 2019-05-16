package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class QueryVmtsRequest extends BaseRequestMsg<QueryVmtsResponse> {

    public QueryVmtsRequest(){
        super("");
        vmtDetailInfos = new ArrayList<>();
    }

    public void addVmtDetailInfos(VmtDetailInfo vmtDetailInfo) {
        vmtDetailInfos.add(vmtDetailInfo);
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        QueryVmtsResponse queryVmtsResponse = new QueryVmtsResponse(code, status.value(), message);
        queryVmtsResponse.setVmts(null);
        ResponseEntity<QueryVmtsResponse> responseResponseEntity = new ResponseEntity<>(queryVmtsResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        QueryVmtsResponse queryVmtsResponse = new QueryVmtsResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        queryVmtsResponse.setVmts(vmtDetailInfos);
        ResponseEntity<QueryVmtsResponse> responseResponseEntity = new ResponseEntity<>(queryVmtsResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    private List<VmtDetailInfo> vmtDetailInfos;
}
