package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;
import java.util.List;

public class SingleChannelPollInfo {
    public SingleChannelPollInfo() {
    }

    public SingleChannelPollInfo(int num, int keepTime) {
        this.num = num;
        this.keepTime = keepTime;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getKeepTime() {
        return keepTime;
    }

    public void setKeepTime(int keepTime) {
        this.keepTime = keepTime;
    }

    public List<Terminal> getMembers() {
        return members;
    }

    public void setMembers(List<Terminal> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("num:").append(num)
                .append(", keepTime:").append(keepTime)
                .append(", members:").append(members.toString())
                .toString();
    }
    private int num; //画面合成批量轮询次数
    private int keepTime; //画面合成批量轮询间隔时间,单位：秒
    List<Terminal> members; //	单通道轮询成员数组
}
