package com.kedacom.confinterface.restclient.mcu;

public class MonitorsMediaFormats {
    public MonitorsMediaFormats(int sampling, int chn_num, int bitrate, int max_frame) {
        this.sampling = sampling;
        this.chn_num = chn_num;
        this.bitrate = bitrate;
        this.max_frame = max_frame;
    }

    public MonitorsMediaFormats() {
    }

    public int getSampling() {
        return sampling;
    }

    public void setSampling(int sampling) {
        this.sampling = sampling;
    }

    public int getChn_num() {
        return chn_num;
    }

    public void setChn_num(int chn_num) {
        this.chn_num = chn_num;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getMax_frame() {
        return max_frame;
    }

    public void setMax_frame(int max_frame) {
        this.max_frame = max_frame;
    }

    private int  sampling; //采样率（监控模式为音频有效）
    private int  chn_num; //声轨数量（监控模式为音频有效
    private int  bitrate; //码率（监控模式为视频有效）
    private int  max_frame; //最大帧率（监控模式为视频有效
}
