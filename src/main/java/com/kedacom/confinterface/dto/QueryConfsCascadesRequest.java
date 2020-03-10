package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class QueryConfsCascadesRequest extends BaseRequestMsg<QueryConfCascadesInfoResponse> {

    public QueryConfsCascadesRequest(String groupId){
        super(groupId);
        cascades = new ArrayList<>();
    }

    public void addConfsCascadesInfo(ConfsCascadesInfo ConfsCascadesInfo) {
        cascades.add(ConfsCascadesInfo);
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        QueryConfCascadesInfoResponse queryConfCascadesInfoResponse = new QueryConfCascadesInfoResponse(code, status.value(), message);
        queryConfCascadesInfoResponse.setCascades(null);
        ResponseEntity<QueryConfCascadesInfoResponse> responseResponseEntity = new ResponseEntity<>(queryConfCascadesInfoResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        QueryConfCascadesInfoResponse queryConfCascadesInfoResponse = new QueryConfCascadesInfoResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        queryConfCascadesInfoResponse.setCascades(cascades);
        ResponseEntity<QueryConfCascadesInfoResponse> responseResponseEntity = new ResponseEntity<>(queryConfCascadesInfoResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    private List<ConfsCascadesInfo> cascades;
}
