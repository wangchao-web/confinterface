package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.restclient.mcu.MtVideos;

import java.util.List;

public class VideoSourceResponse extends  BaseResponseMsg {

    public VideoSourceResponse(int code, int status, String message) {
        super(code, status, message);
    }

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

    private int curVideoIdx;

    private List<MtVideos> mtVideos;

}
