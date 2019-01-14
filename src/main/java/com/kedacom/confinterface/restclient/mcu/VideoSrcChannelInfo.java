package com.kedacom.confinterface.restclient.mcu;

public class VideoSrcChannelInfo extends BaseVideoChannelInfo {
    public int getCur_video_idx() {
        return cur_video_idx;
    }

    public void setCur_video_idx(int cur_video_idx) {
        this.cur_video_idx = cur_video_idx;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(", cur_video_idx:").append(cur_video_idx).toString();
    }

    private int cur_video_idx;
}
