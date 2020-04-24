package com.kedacom.confinterface.restclient.mcu;

import com.kedacom.confinterface.dto.SingleChannelPollInfo;


public class McuVmpsMembersInfo {

    public McuVmpsMembersInfo(int chn_idx, int member_type) {
        this.chn_idx = chn_idx;
        this.member_type = member_type;
    }

    public McuVmpsMembersInfo(int chn_idx, int member_type, String mt_id, MCuSingChannelPollInfo poll) {
        this.chn_idx = chn_idx;
        this.member_type = member_type;
        this.mt_id = mt_id;
        this.poll = poll;
    }

    public McuVmpsMembersInfo() {
    }

    public int getChn_idx() {
        return chn_idx;
    }

    public void setChn_idx(int chn_idx) {
        this.chn_idx = chn_idx;
    }

    public int getMember_type() {
        return member_type;
    }

    public void setMember_type(int member_type) {
        this.member_type = member_type;
    }

    public String getMt_id() {
        return mt_id;
    }

    public void setMt_id(String mt_id) {
        this.mt_id = mt_id;
    }

    public MCuSingChannelPollInfo getPoll() {
        return poll;
    }

    public void setPoll(MCuSingChannelPollInfo poll) {
        this.poll = poll;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("chn_idx:").append(chn_idx)
                .append(", member_type:").append(member_type)
                .append(", mt_id:").append(mt_id)
                .toString();
    }

    private int chn_idx; //画面合成通道索引，从0开始
    private int member_type; //成员类型 1-会控指定；2-发言人跟随；3-主席跟随；4-轮询跟随；6-单通道轮询；7-双流跟随；
    private String mt_id; //  通道终端号，仅当通道中为会控指定时需要 最大字符长度：48个字节
    private MCuSingChannelPollInfo poll; //单通道轮询参数，仅member_type为6时有效
}
