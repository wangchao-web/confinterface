package com.kedacom.confinterface.inner;

public enum TransportDirectionEnum {
    SEND(1, "send"), RECV(2, "recv"), SENDRECV(3, "sendrecv");

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    TransportDirectionEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    private int code;
    private String name;
}
