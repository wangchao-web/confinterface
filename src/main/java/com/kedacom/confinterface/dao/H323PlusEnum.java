package com.kedacom.confinterface.dao;

public enum H323PlusEnum {
    SUCCESS(0,"success"),FAILED(1,"failed"), UNKNOWN(2, "unknown");


    H323PlusEnum(int code, String typeName){
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
