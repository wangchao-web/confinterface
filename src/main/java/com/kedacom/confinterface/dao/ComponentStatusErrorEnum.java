package com.kedacom.confinterface.dao;

public enum ComponentStatusErrorEnum {
    UNKNOWN(0, "unknown"),
    LOGINFAILED(1,"login failed"),
    TOKENFAILED(2,"get token failed"),
    GKREGISTERFAILED(3,"gk register failed");

    ComponentStatusErrorEnum(int code, String message){
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private int code;
    private String message;
}
