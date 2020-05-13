package com.kedacom.confinterface.dto;

import org.hibernate.validator.constraints.Range;

public class MonitorsParams {

    public MonitorsParams(@Range(min = 0, max = 1) int mode, @Range(min = 1, max = 3) int type, String e164) {
        this.mode = mode;
        this.type = type;
        this.e164 = e164;
    }

    public MonitorsParams() {
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getE164() {
        return e164;
    }

    public void setE164(String e164) {
        this.e164 = e164;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("mode:").append(mode)
                .append(", type:").append(type)
                .append(", E164:").append(e164)
                .toString();
    }
    @Range(min = 0, max = 1)
    private int mode; //监控模式 0-视频；1-音频；
    @Range(min = 1, max = 3)
    private int type; //监控类型1-终端；2-画面合成；3-混音；
    private String e164; // 监控终端E164, type为终端时必填
}
