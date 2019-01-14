package com.kedacom.confinterface.restclient.mcu;

public enum InspectionStatusEnum {
    UNKNOWN(0, "unknown"), OK(1, "OK"), FAIL(2, "Fail"), CANCELOK(3, "Cancel Ok");

    InspectionStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    private int code;
    private String name;
}
