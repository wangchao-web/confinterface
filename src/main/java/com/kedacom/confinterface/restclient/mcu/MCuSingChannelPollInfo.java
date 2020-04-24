package com.kedacom.confinterface.restclient.mcu;

import java.util.List;

public class MCuSingChannelPollInfo {
    public MCuSingChannelPollInfo(int num, int keep_time) {
        this.num = num;
        this.keep_time = keep_time;
    }

    public MCuSingChannelPollInfo(int num, int keep_time, List<TerminalId> members) {
        this.num = num;
        this.keep_time = keep_time;
        this.members = members;
    }

    public MCuSingChannelPollInfo() {
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getKeep_time() {
        return keep_time;
    }

    public void setKeep_time(int keep_time) {
        this.keep_time = keep_time;
    }

    public List<TerminalId> getMembers() {
        return members;
    }

    public void setMembers(List<TerminalId> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("num:").append(num)
                .append(", keep_time:").append(keep_time)
                .append(", members:").append(members.toString())
                .toString();
    }
    private int num; //画面合成批量轮询次数
    private int keep_time; //画面合成批量轮询间隔时间,单位：秒
    List<TerminalId> members; //	单通道轮询成员数组
}
