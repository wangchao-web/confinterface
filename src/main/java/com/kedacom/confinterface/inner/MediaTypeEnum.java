package com.kedacom.confinterface.inner;

public enum MediaTypeEnum {
    VIDEO(1, "video"), AUDIO(2, "audio"), ALL(3, "all"), UNKNOWN(4, "unknown");

    MediaTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    private int code;
    private String name;
}
