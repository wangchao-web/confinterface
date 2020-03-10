package com.kedacom.confinterface.dto;

public class QueryConfMtInfoResponse extends  BaseResponseMsg{
    public QueryConfMtInfoResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public ConfMtInfo getConfMtInfo() {
        return confMtInfo;
    }

    public void setConfMtInfo(ConfMtInfo confMtInfo) {
        this.confMtInfo = confMtInfo;
    }

    private ConfMtInfo confMtInfo;
}
