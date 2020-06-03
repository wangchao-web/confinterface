package com.kedacom.confinterface.dto;

import javax.validation.constraints.NotNull;

public class P2PAudioCallMediaCap {
    public String getCodecFormat() {
        return codecFormat;
    }

    public void setCodecFormat(String codecFormat) {
        this.codecFormat = codecFormat;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getChannelNum() {
        return channelNum;
    }

    public void setChannelNum(int channelNum) {
        this.channelNum = channelNum;
    }

    public int getSampleRate() {
        return samplerate;
    }

    public void setSampleRate(int sampleRate) {
        this.samplerate = sampleRate;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("codecFormat:").append(codecFormat)
                .append(", bitrate:").append(bitrate)
                .append(", channelNum:").append(channelNum)
                .append(", sampleRate:").append(samplerate).toString();
    }

    @NotNull
    private String codecFormat;
    private int bitrate;
    private int channelNum ;
    private int samplerate ;
}
