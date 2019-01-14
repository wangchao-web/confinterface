package com.kedacom.confinterface.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class InspectionRequest extends BaseRequestMsg<InspectionResponse> {

    public InspectionRequest(String groupId, InspectionParam inspectionParam) {
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
        InspectionResponse inspectionResponse = new InspectionResponse(code, status.value(), message);
        inspectionResponse.setResources(null);
        ResponseEntity<InspectionResponse> responseEntity = new ResponseEntity<>(inspectionResponse, status);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeSuccessResponseMsg() {
        makeSuccessResponseMsg(null);
    }

    public void makeSuccessResponseMsg(List<MediaResource> resources) {
        InspectionResponse inspectionResponse = new InspectionResponse(0, HttpStatus.OK.value(), "Ok");
        inspectionResponse.setResources(resources);
        ResponseEntity<InspectionResponse> responseEntity = new ResponseEntity<>(inspectionResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    private InspectionParam inspectionParam;
}
