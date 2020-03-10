package com.kedacom.confinterface.restclient.mcu;

public class TerminalIdInfo {
    public TerminalIdInfo(String mt_id) {
        this.mt_id = mt_id;
    }

    public TerminalIdInfo() {
    }

    public String getMt_id() {
        return mt_id;
    }

    public void setMt_id(String mt_id) {
        this.mt_id = mt_id;
    }

    @Override
    public String toString() {
        return mt_id;
    }


    private String mt_id;  //终端号 最大字符长度：48个字节
}
