package com.kedacom.confinterface.restclient.mcu;

import java.util.List;

public class GetConfMtInfoResponse  extends McuBaseResponse{
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

    public int getInspection() {
        return inspection;
    }

    public void setInspection(int inspection) {
        this.inspection = inspection;
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

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public int getCall_mode() {
        return call_mode;
    }

    public void setCall_mode(int call_mode) {
        this.call_mode = call_mode;
    }

    public int getRcv_volume() {
        return rcv_volume;
    }

    public void setRcv_volume(int rcv_volume) {
        this.rcv_volume = rcv_volume;
    }

    public int getSnd_volume() {
        return snd_volume;
    }

    public void setSnd_volume(int snd_volume) {
        this.snd_volume = snd_volume;
    }

    public List<VideoSrcChannelInfo> getV_snd_chn() {
        return v_snd_chn;
    }

    public void setV_snd_chn(List<VideoSrcChannelInfo> v_snd_chn) {
        this.v_snd_chn = v_snd_chn;
    }

    public List<BaseVideoChannelInfo> getV_rcv_chn() {
        return v_rcv_chn;
    }

    public void setV_rcv_chn(List<BaseVideoChannelInfo> v_rcv_chn) {
        this.v_rcv_chn = v_rcv_chn;
    }

    public List<BaseVideoChannelInfo> getDv_snd_chn() {
        return dv_snd_chn;
    }

    public void setDv_snd_chn(List<BaseVideoChannelInfo> dv_snd_chn) {
        this.dv_snd_chn = dv_snd_chn;
    }

    public List<BaseVideoChannelInfo> getDv_rcv_chn() {
        return dv_rcv_chn;
    }

    public void setDv_rcv_chn(List<BaseVideoChannelInfo> dv_rcv_chn) {
        this.dv_rcv_chn = dv_rcv_chn;
    }

    public List<BaseChannelInfo> getA_snd_chn() {
        return a_snd_chn;
    }

    public void setA_snd_chn(List<BaseChannelInfo> a_snd_chn) {
        this.a_snd_chn = a_snd_chn;
    }

    public List<BaseChannelInfo> getA_rcv_chn() {
        return a_rcv_chn;
    }

    public void setA_rcv_chn(List<BaseChannelInfo> a_rcv_chn) {
        this.a_rcv_chn = a_rcv_chn;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("alias:").append(alias)
                .append(", mt_id:").append(mt_id)
                .append(", ip:").append(ip)
                .append(", online:").append(online)
                .append(", e164:").append(e164)
                .append(", type:").append(type)
                .append(", bitrate:").append(bitrate)
                .append(", silence:").append(silence)
                .append(", mute:").append(mute)
                .append(", dual:").append(dual)
                .append(", mix:").append(mix)
                .append(", vmp:").append(vmp)
                .append(", inspection:").append(inspection)
                .append(", rec:").append(rec)
                .append(", poll:").append(poll)
                .append(", upload:").append(upload)
                .append(", protocol:").append(protocol)
                .append(", call_mode:").append(call_mode)
                .append(", rcv_volume:").append(rcv_volume)
                .append(", snd_volume:").append(snd_volume)
                .append(", v_snd_chn:").append(v_snd_chn)
                .append(", v_rcv_chn:").append(v_rcv_chn)
                .append(", dv_snd_chn:").append(dv_snd_chn)
                .append(", dv_rcv_chn:").append(dv_rcv_chn)
                .append(", a_snd_chn:").append(a_snd_chn)
                .append(", a_rcv_chn:").append(a_rcv_chn)
                .toString();
    }

    private String alias;
    private String mt_id;
    private String ip;
    private int online;
    private String e164;
    private int type;   //终端类型, 1-普通终端, 3-电话终端, 5-卫星终端, 7-下级会议, 8-上级会议
    private int bitrate;
    private int silence;
    private int mute;
    private int dual;
    private int mix;
    private int vmp;
    private int inspection;
    private int rec;
    private int poll;
    private int upload;
    private int protocol;
    private int call_mode;
    private int rcv_volume;
    private int snd_volume;
    private List<VideoSrcChannelInfo> v_snd_chn;
    private List<BaseVideoChannelInfo> v_rcv_chn;
    private List<BaseVideoChannelInfo> dv_snd_chn;
    private List<BaseVideoChannelInfo> dv_rcv_chn;
    private List<BaseChannelInfo> a_snd_chn;
    private List<BaseChannelInfo> a_rcv_chn;
}
