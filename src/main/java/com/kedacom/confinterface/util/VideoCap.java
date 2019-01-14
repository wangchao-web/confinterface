package com.kedacom.confinterface.util;

public class VideoCap {

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public int getResolution() {
        return resolution;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRateType(int bitRateType) {
        this.bitRateType = bitRateType;
    }

    public int getBitRateType() {
        return bitRateType;
    }

    private int mediaType;
    private int resolution;
    private int frameRate;
    private int bitRate;
    private int bitRateType;
}
