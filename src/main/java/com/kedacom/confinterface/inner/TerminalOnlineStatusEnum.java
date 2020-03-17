package com.kedacom.confinterface.inner;

public enum TerminalOnlineStatusEnum {
    UNKNOWN(0, "unknown"), ONLINE(1, "online"), OFFLINE(2, "offline"), OCCUPIED(3, "occupied"),
    UNREGISTERED(4, "unregistered"), DUALSTREAM(5, "dualstream"),
    LEAVECONFERENCE(6, "leaveconference");
    public int getCode(){
        return this.code;
    }

    TerminalOnlineStatusEnum(int code, String name){
        this.code = code;
        this.name = name;
    }

    private int code;
    private String name;
}
