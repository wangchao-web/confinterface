package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.util.ConfInterfaceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.security.auth.login.Configuration;
import java.util.ArrayList;
import java.util.List;

public class TestStringRequest extends BaseRequestMsg<TestStringResponse> {

    public TestStringRequest() {
        super("");
        this.testStrings = new ArrayList<>();
    }

    public List<String> getTestStrings() {
        return testStrings;
    }

    public void setTestStrings(List<String> testStrings) {
        this.testStrings = testStrings;
    }

    @Override
    public void makeSuccessResponseMsg() {
        TestStringResponse testStringResponse = new TestStringResponse(ConfInterfaceResult.OK.getCode(), HttpStatus.OK.value(), ConfInterfaceResult.OK.getMessage());
        testStringResponse.setTestStrings(testStrings);
        ResponseEntity<TestStringResponse> responseEntity = new ResponseEntity<>(testStringResponse, HttpStatus.OK);
        responseMsg.setResult(responseEntity);
    }

    @Override
    public void makeErrorResponseMsg(int code, HttpStatus status, String message) {
        TestStringResponse testStringResponse = new TestStringResponse(code, status.value(), message);
        testStringResponse.setTestStrings(testStrings);
        ResponseEntity<TestStringResponse> responseEntity = new ResponseEntity<>(testStringResponse, status);
        responseMsg.setResult(responseEntity);
    }

    private List<String> testStrings;
}
