package com.kedacom.confinterface.restclient.mcu;

public enum SubscribeMethodEnum {
    UPDATE("update"), DELETE("delete"), NOTIFY("notify");

    SubscribeMethodEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
}
