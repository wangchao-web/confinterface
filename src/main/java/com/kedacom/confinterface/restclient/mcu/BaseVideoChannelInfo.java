package com.kedacom.confinterface.restclient.mcu;

public class BaseVideoChannelInfo extends BaseChannelInfo {
    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public boolean valid(){
        if (bitrate == 0)
            return false;

        if (resolution == 0)
            return false;

        if (format == 0)
            return false;

        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(", bitrate:").append(bitrate)
                .append(", resolution:").append(resolution)
                .append(", format:").append(format).toString();
    }

    private int bitrate;
    private int resolution;
    private int format;   //视频格式， 1-MPEG;2-H.261;3-H.263;4-H.264_HP;5-H.264_BP;6-H.265;
}
