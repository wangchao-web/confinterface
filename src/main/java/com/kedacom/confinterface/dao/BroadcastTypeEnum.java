package com.kedacom.confinterface.dao;

public enum BroadcastTypeEnum {
    UNKNOWN(0,"unknown"),TERMINAL(1,"terminal"), OTHER(2, "other");


    BroadcastTypeEnum(int code, String typeName){
        this.code = code;
        this.typeName = typeName;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    private int code;
    private String typeName;
}
