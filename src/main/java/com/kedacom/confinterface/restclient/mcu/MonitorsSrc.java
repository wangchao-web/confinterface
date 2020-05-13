package com.kedacom.confinterface.restclient.mcu;

public class MonitorsSrc {

    public MonitorsSrc(int type, String mt_id) {
        this.type = type;
        this.mt_id = mt_id;
    }

    public MonitorsSrc() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMt_id() {
        return mt_id;
    }

    public void setMt_id(String mt_id) {
        this.mt_id = mt_id;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("type:").append(type)
                .append(", mt_id:").append(mt_id)
                .toString();
    }

    /**
     * type 监控类型
     * 1-终端；
     * 2-画面合成；
     * 3-混音；
     */
    private int type;

    /**
     * mt_id 监控终端id, type为终端时必填 最大字符长度：48个字节
     */
    private String mt_id;
}
