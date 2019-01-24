package com.kedacom.confinterface.dto;

public class StartDualResponse extends BaseResponseMsg {

    public StartDualResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    private String resourceId;
}
