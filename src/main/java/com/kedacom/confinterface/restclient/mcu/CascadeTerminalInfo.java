package com.kedacom.confinterface.restclient.mcu;

public class CascadeTerminalInfo {
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
                .append(", ip:").append(ip)
                .append(",online:").append(online)
                .append(",e164:").append(e164)
                .append(",type:").append(type)
                .append(",bitrate:").append(bitrate)
                .append(",silence:").append(silence)
                .append(",mute:").append(mute)
                .append(",dual:").append(dual)
                .append(",mix:").append(mix)
                .append(",vmp:").append(vmp)
                .append(",rec:").append(rec)
                .append(",poll:").append(poll)
                .append(",upload:").append(upload)
                .toString();
    }

    private String alias;
    private String mt_id;
    private String ip;
    private int online;
    private String e164;
    private int type;
    private int bitrate;
    private int silence;
    private int mute;
    private int dual;
    private int mix;
    private int vmp;   //是否在合成
    private int rec;
    private int poll;  //是否在轮巡
    private int upload;  //是否在上传
}
