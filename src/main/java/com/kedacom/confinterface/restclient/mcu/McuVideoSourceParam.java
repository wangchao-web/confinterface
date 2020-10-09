package com.kedacom.confinterface.restclient.mcu;

public class McuVideoSourceParam {


    public McuVideoSourceParam(int video_idx) {
        this.video_idx = video_idx;
    }

    public McuVideoSourceParam() {
    }

    public int getVideo_idx() {
        return video_idx;
    }

    public void setVideo_idx(int video_idx) {
        this.video_idx = video_idx;
    }

    private int video_idx;
}
