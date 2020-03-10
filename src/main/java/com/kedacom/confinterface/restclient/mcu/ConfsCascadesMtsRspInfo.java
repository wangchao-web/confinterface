package com.kedacom.confinterface.restclient.mcu;

public class ConfsCascadesMtsRspInfo {


    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getMt_id() {
        return mt_id;
    }

    public void setMt_id(String mt_id) {
        this.mt_id = mt_id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public String getE164() {
        return e164;
    }

    public void setE164(String e164) {
        this.e164 = e164;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getSilence() {
        return silence;
    }

    public void setSilence(int silence) {
        this.silence = silence;
    }

    public int getMute() {
        return mute;
    }

    public void setMute(int mute) {
        this.mute = mute;
    }

    public int getDual() {
        return dual;
    }

    public void setDual(int dual) {
        this.dual = dual;
    }

    public int getMix() {
        return mix;
    }

    public void setMix(int mix) {
        this.mix = mix;
    }

    public int getVmp() {
        return vmp;
    }

    public void setVmp(int vmp) {
        this.vmp = vmp;
    }

    public int getRec() {
        return rec;
    }

    public void setRec(int rec) {
        this.rec = rec;
    }

    public int getPoll() {
        return poll;
    }

    public void setPoll(int poll) {
        this.poll = poll;
    }

    public int getUpload() {
        return upload;
    }

    public void setUpload(int upload) {
        this.upload = upload;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("alias:").append(alias)
                .append(", mt_id:").append(mt_id)
                .append(", ip:") .append(ip)
                .append(", online:") .append(online)
                .append(", type:") .append(type)
                .append(", bitrate:") .append(bitrate)
                .append(", e164:") .append(e164)
                .toString();
    }

    private String  alias; //终端别名 最大字符长度：128个字节
    private String mt_id;  //终端号 最大字符长度：48个字节
    private String ip;     //终端IP
    private int online;    //是否在线 0否 ,1是
    private String e164;
    private int type;      //终端类型1-普通终端3-电话终端；5-卫星终端；7-下级会议；8-上级会议
    private int bitrate;
    private int silence;
    private int mute;
    private int dual;
    private int mix;
    private int vmp;
    private int rec;
    private int poll;
    private int upload;
}
