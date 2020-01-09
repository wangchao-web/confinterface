package com.kedacom.confinterface.dto;

import javax.validation.constraints.NotNull;

public class P2PCallMediaCap {
    public P2PCallMediaCap(@NotNull String codecFormat, @NotNull String resolution, int bitrate, int framerate) {
        this.codecFormat = codecFormat;
        this.resolution = resolution;
        this.bitrate = bitrate;
        this.framerate = framerate;
    }

    public P2PCallMediaCap() {
    }

    public String getCodecFormat() {
        return codecFormat;
    }

    public void setCodecFormat(String codecFormat) {
        this.codecFormat = codecFormat;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getFramerate() {
        return framerate;
    }

    public void setFramerate(int framerate) {
        this.framerate = framerate;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("codecFormat:").append(codecFormat)
                .append(", resolution:").append(resolution)
                .append(", bitrate:").append(bitrate)
                .append(", framerate:").append(framerate)
                .toString();
    }

    @NotNull
    private String codecFormat;
    @NotNull
    private String resolution;
    private int bitrate ;
    private int framerate ;
}
