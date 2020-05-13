package com.kedacom.confinterface.restclient.mcu;


public class McuStartMonitorsParam  {
    public McuStartMonitorsParam(int mode, MonitorsSrc src, VideoFormat video_format, AudioFormat audio_format, McuMonitorsDst dst) {
        this.mode = mode;
        this.src = src;
        this.video_format = video_format;
        this.audio_format = audio_format;
        this.dst = dst;
    }

    public McuStartMonitorsParam() {
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public MonitorsSrc getSrc() {
        return src;
    }

    public void setSrc(MonitorsSrc src) {
        this.src = src;
    }

    public VideoFormat getVideo_format() {
        return video_format;
    }

    public void setVideo_format(VideoFormat video_format) {
        this.video_format = video_format;
    }

    public AudioFormat getAudio_format() {
        return audio_format;
    }

    public void setAudio_format(AudioFormat audio_format) {
        this.audio_format = audio_format;
    }

    public McuMonitorsDst getDst() {
        return dst;
    }

    public void setDst(McuMonitorsDst dst) {
        this.dst = dst;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("mode:").append(mode)
                .append(", MonitorsSrc:").append(src.toString())
                .append(", VideoFormat:").append(video_format.toString())
                .append(", AudioFormat:").append(audio_format.toString())
                .append(", McuMonitorsDst:").append(dst.toString())
                .toString();
    }

    private int mode;
    private MonitorsSrc src;
    private VideoFormat video_format;
    private AudioFormat audio_format;
    private McuMonitorsDst dst;

}
