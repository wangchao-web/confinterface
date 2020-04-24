package com.kedacom.confinterface.restclient.mcu;

import java.util.ArrayList;

public class McuGetVmpsInfoResponse extends McuBaseResponse{

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public int getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(int broadcast) {
        this.broadcast = broadcast;
    }

    public int getVoice_hint() {
        return voice_hint;
    }

    public void setVoice_hint(int voice_hint) {
        this.voice_hint = voice_hint;
    }

    public int getShow_mt_name() {
        return show_mt_name;
    }

    public void setShow_mt_name(int show_mt_name) {
        this.show_mt_name = show_mt_name;
    }

    public McuMtNamStyle getMt_name_style() {
        return mt_name_style;
    }

    public void setMt_name_style(McuMtNamStyle mt_name_style) {
        this.mt_name_style = mt_name_style;
    }

    public ArrayList<McuVmpsMembersInfo> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<McuVmpsMembersInfo> members) {
        this.members = members;
    }

    public MCuSingChannelPollInfo getPoll() {
        return poll;
    }

    public void setPoll(MCuSingChannelPollInfo poll) {
        this.poll = poll;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("mode:").append(mode)
                .append(", layout:").append(layout)
                .append(", broadcast:").append(broadcast)
                .append(", voice_hint:").append(voice_hint)
                .append(", show_mt_name:").append(show_mt_name)
                .append(", mtNameStyle:").append(mt_name_style.toString())
                .append(", members:").append(members.toString())
                .append(", poll:").append(poll.toString())
                .toString();
    }

    private int mode; // 1定制画面合成2-自动画面合成；3-自动画面合成批量轮询；4-定制画面合成批量轮询；
    private int layout; // 画面合成风格
    private int broadcast; //是否广播 0-否；1-是；
    private int voice_hint; //是否识别声音来源 0-否；1-是
    private int show_mt_name; //是否显示别名 0-否；1-是；
    private McuMtNamStyle mt_name_style; //画面合成台标参数
    private ArrayList<McuVmpsMembersInfo> members; //画面合成成员数组
    private MCuSingChannelPollInfo poll; //画面合成轮询信息,仅mode为3、4时有效

}