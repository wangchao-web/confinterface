package com.kedacom.confinterface.restclient.mcu;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MtVideos {

    public int getVideoIdx() {
        return videoIdx;
    }

    public void setVideoIdx(int videoIdx) {
        this.videoIdx = videoIdx;
    }

    public String getVideoAlise() {
        return videoAlise;
    }

    public void setVideoAlise(String videoAlise) {
        this.videoAlise = videoAlise;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("videoIdx:").append(videoIdx).append(",videoAlise:").append(videoAlise).toString();
    }
    @JsonProperty(value = "video_idx")
    private int videoIdx;  //视频源通道号

    @JsonProperty(value = "video_alise")
    private String videoAlise; //视频源别名

}
