package com.kedacom.confinterface.dto;

import java.util.List;

public class TestStringResponse extends BaseResponseMsg {
    public TestStringResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public void setTestStrings(List<String> testStrings) {
        this.testStrings = testStrings;
    }

    public List<String> getTestStrings() {
        return testStrings;
    }

    List<String> testStrings;
}
