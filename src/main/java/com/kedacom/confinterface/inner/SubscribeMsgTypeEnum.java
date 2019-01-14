package com.kedacom.confinterface.inner;

public enum SubscribeMsgTypeEnum {
    TERMINAL_STATUS(1, "terminal status");

    public int getType() {
        return type;
    }

    public String getName(){
        return name;
    }

    SubscribeMsgTypeEnum(int type, String name){
        this.type = type;
        this.name = name;
    }

    private int type;
    private String name;
}
