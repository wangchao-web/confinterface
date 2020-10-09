package com.kedacom.confinterface.restclient.mcu;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class GetVideoSourceResponse extends McuBaseResponse {

    public int getCurVideoIdx() {
        return curVideoIdx;
    }

    public void setCurVideoIdx(int curVideoIdx) {
        this.curVideoIdx = curVideoIdx;
    }

    public List<MtVideos> getMtVideos() {
        return mtVideos;
    }

    public void setMtVideos(List<MtVideos> mtVideos) {
        this.mtVideos = mtVideos;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("curVideoIdx:").append(curVideoIdx).append(",mtVideos:").append(mtVideos).toString();
    }
    @JsonProperty(value = "cur_video_idx")
    private int curVideoIdx;  //当前终端的视频源通道号

    @JsonProperty(value = "mt_videos")
    private List<MtVideos> mtVideos; //终端视频源数组，最多返回10个视频源
}
