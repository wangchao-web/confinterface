package com.kedacom.confinterface.dto;

public class QueryConfInfoResponse extends   BaseResponseMsg{

    public QueryConfInfoResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public ConfInfo getConfInfo() {
        return confInfo;
    }

    public void setConfInfo(ConfInfo confInfo) {
        this.confInfo = confInfo;
    }

    private ConfInfo confInfo;
}
