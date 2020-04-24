package com.kedacom.confinterface.restclient.mcu;

import java.util.List;

public class SendSmsInfo {

    public SendSmsInfo(String message, int type, int roll_num, int roll_speed) {
        this.message = message;
        this.type = type;
        this.roll_num = roll_num;
        this.roll_speed = roll_speed;
    }

    public SendSmsInfo() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRoll_num() {
        return roll_num;
    }

    public void setRoll_num(int roll_num) {
        this.roll_num = roll_num;
    }

    public int getRoll_speed() {
        return roll_speed;
    }

    public void setRoll_speed(int roll_speed) {
        this.roll_speed = roll_speed;
    }

    public List<TerminalId> getMts() {
        return mts;
    }

    public void setMts(List<TerminalId> mts) {
        this.mts = mts;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("message:").append(message)
                .append(",type:").append(type)
                .append(",roll_num:").append(roll_num)
                .append(",roll_speed:").append(roll_speed)
                .append(",mts:").append(mts)
                .toString();
    }

    private  String message; //消息内容,发送空消息即停止短消息 最大字符长度：1500个字节
    private int type;         //短消息类型0-自右至左滚动；1-翻页滚动；2-全页滚动；
    private int roll_num;    //滚动次数 1-255 新版本终端255为无限轮询
    private int roll_speed;   //滚动速度1-慢速；2-中速；3-快速；
    private List<TerminalId> mts;
}
