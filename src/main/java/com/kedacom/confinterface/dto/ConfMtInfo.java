package com.kedacom.confinterface.dto;

public class ConfMtInfo {

    public ConfMtInfo(String e164, String ip, int type, int online, String alias, int bitrate, String mtId, int silence, int mute, int inspection) {
        this.e164 = e164;
        this.ip = ip;
        this.type = type;
        this.online = online;
        this.alias = alias;
        this.bitrate = bitrate;
        this.mtId = mtId;
        this.silence = silence;
        this.mute = mute;
        this.inspection = inspection;
    }

    public ConfMtInfo() {
    }

    public String getE164() {
        return e164;
    }

    public void setE164(String e164) {
        this.e164 = e164;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getMtId() {
        return mtId;
    }

    public void setMtId(String mtId) {
        this.mtId = mtId;
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

    public int getInspection() {
        return inspection;
    }

    public void setInspection(int inspection) {
        this.inspection = inspection;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("e164:").append(e164)
                .append(", ip:").append(ip)
                .append(", type:").append(type)
                .append(", online:").append(online)
                .append(", alias:").append(alias)
                .append(", bitrate:").append(bitrate)
                .append(", mtId:").append(mtId)
                .append(", silence:").append(silence)
                .append(", mute:").append(mute)
                .append(", inspection:").append(inspection)
                .toString();
    }

    private String e164;   //E164号
    private String ip;
    private int type; //终端类型: 1-普通终端；3-电话终端；5-卫星终端；7-下级会议；8-上级会议；
    private int online;
    private String alias;
    private int bitrate ;
    private String mtId; //终端号 最大字符长度：48个字节
    private int silence ; //是否静音0-否；1-是；
    private int mute; //是否哑音0-否; 1-是；
    private int inspection; //是否在选看0-否; 1-是；
}
