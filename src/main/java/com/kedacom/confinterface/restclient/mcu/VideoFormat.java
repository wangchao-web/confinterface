package com.kedacom.confinterface.restclient.mcu;

public class VideoFormat {
    public VideoFormat(int format, int resolution, int frame, int bitrate) {
        this.format = format;
        this.resolution = resolution;
        this.frame = frame;
        this.bitrate = bitrate;
    }

    public VideoFormat() {
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    @Override
    public String toString() {
        return new StringBuffer()
                .append("format:")
                .append(format)
                .append(",resolution:")
                .append(resolution)
                .append(",frame")
                .append(frame)
                .append(",bitrate:")
                .append(bitrate)
                .toString();
    }

    private int format;
    private int resolution;
    private int frame;
    private int bitrate;
}
