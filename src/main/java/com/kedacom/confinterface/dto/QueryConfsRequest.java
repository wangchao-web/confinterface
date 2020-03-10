package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class QueryConfsRequest extends BaseRequestMsg<QueryConfsResponse>{

    public QueryConfsRequest(){
        super("");
        confsDetailInfos = new ArrayList<>();
    }

    public void addConfDetailInfos(ConfsDetailInfo confsDetailInfo) {
        confsDetailInfos.add(confsDetailInfo);
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        QueryConfsResponse queryConfsResponse = new QueryConfsResponse(code, status.value(), message);
        queryConfsResponse.setConfs(null);
        ResponseEntity<QueryConfsResponse> responseResponseEntity = new ResponseEntity<>(queryConfsResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        QueryConfsResponse queryConfsResponse = new QueryConfsResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        queryConfsResponse.setConfs(confsDetailInfos);
        ResponseEntity<QueryConfsResponse> responseResponseEntity = new ResponseEntity<>(queryConfsResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    private List<ConfsDetailInfo> confsDetailInfos;
}
