package com.kedacom.confinterface.dto;

import org.hibernate.validator.constraints.Range;

public class VmpsMembersInfo {
    public VmpsMembersInfo(int chnIdx, @Range(min = 1, max = 7) int memberType, String mtE164, SingleChannelPollInfo poll) {
        this.chnIdx = chnIdx;
        this.memberType = memberType;
        this.mtE164 = mtE164;
        this.poll = poll;
    }

    public VmpsMembersInfo(int chnIdx, @Range(min = 1, max = 7) int memberType) {
        this.chnIdx = chnIdx;
        this.memberType = memberType;
    }

    public VmpsMembersInfo() {
    }

    public int getChnIdx() {
        return chnIdx;
    }

    public void setChnIdx(int chnIdx) {
        this.chnIdx = chnIdx;
    }

    public int getMemberType() {
        return memberType;
    }

    public void setMemberType(int memberType) {
        this.memberType = memberType;
    }

    public String getMtE164() {
        return mtE164;
    }

    public void setMtE164(String mtE164) {
        this.mtE164 = mtE164;
    }

    public SingleChannelPollInfo getPoll() {
        return poll;
    }

    public void setPoll(SingleChannelPollInfo poll) {
        this.poll = poll;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("chnIdx:").append(chnIdx)
                .append(", memberType:").append(memberType)
                .append(", mtE164:").append(mtE164)
                .toString();
    }

    private int chnIdx ; //画面合成通道索引，从0开始

    @Range(min = 1,max = 7)
    private int memberType ; //成员类型 1-会控指定；2-发言人跟随；3-主席跟随；4-轮询跟随；6-单通道轮询；7-双流跟随；

    private String mtE164 ; //  通道终端号，仅当通道中为会控指定时需要 最大字符长度：48个字节

    private SingleChannelPollInfo poll ; //单通道轮询参数，仅member_type为6时有效
}
