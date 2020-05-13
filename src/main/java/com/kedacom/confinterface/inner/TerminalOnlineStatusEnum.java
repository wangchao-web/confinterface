package com.kedacom.confinterface.inner;

public enum TerminalOnlineStatusEnum {
    UNKNOWN(0, "unknown"), ONLINE(1, "online"), OFFLINE(2, "offline"), OCCUPIED(3, "occupied"),
    UNREGISTERED(4, "unregistered"), DUALSTREAM(5, "dualstream"),
    LEAVECONFERENCE(6, "leave conference"), STATECHANGE(7,"state change"),
    VMPSCHANGE(8,"vmps state change"),VMPSDELETE(9,"vmps delete change"),MIXSCHANGE(10,"mixs state change"),
    MIXSDELETE(11,"mixs delete change");
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
