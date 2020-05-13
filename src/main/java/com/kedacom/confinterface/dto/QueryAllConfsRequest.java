package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class QueryAllConfsRequest extends BaseRequestMsg<QueryAllConfsResponse>{

    public QueryAllConfsRequest(){
        super("");
        confsDetailInfos = new ArrayList<>();
    }

    public void addConfDetailInfos(ConfsDetailInfo confsDetailInfo) {
        confsDetailInfos.add(confsDetailInfo);
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        QueryAllConfsResponse queryAllConfsResponse = new QueryAllConfsResponse(code, status.value(), message);
        queryAllConfsResponse.setConfs(null);
        ResponseEntity<QueryAllConfsResponse> responseResponseEntity = new ResponseEntity<>(queryAllConfsResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        QueryAllConfsResponse queryAllConfsResponse = new QueryAllConfsResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        queryAllConfsResponse.setConfs(confsDetailInfos);
        ResponseEntity<QueryAllConfsResponse> responseResponseEntity = new ResponseEntity<>(queryAllConfsResponse, HttpStatus.OK);
        responseMsg.setResult(responseResponseEntity);
    }

    private List<ConfsDetailInfo> confsDetailInfos;
}
