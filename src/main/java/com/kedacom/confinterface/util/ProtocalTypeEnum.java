package com.kedacom.confinterface.util;

public enum ProtocalTypeEnum {

    H323(0, "h323"), SIP(1, "sip"),H323Plus(2, "H323Plus");

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setH323() {
        this.name = H323.name;
    }

    public void setSip() {
        this.name = SIP.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    ProtocalTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    private int code;
    private String name;
}
