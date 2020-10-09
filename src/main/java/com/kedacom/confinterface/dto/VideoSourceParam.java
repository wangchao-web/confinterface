package com.kedacom.confinterface.dto;


import javax.validation.constraints.NotEmpty;

public class VideoSourceParam {
    public int getVideoIdx() {
        return videoIdx;
    }

    public void setVideoIdx(int videoIdx) {
        this.videoIdx = videoIdx;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
    @Override
    public String toString() {
        return new StringBuilder().append("videoIdx : ").append(videoIdx).append(", account :").append(account).toString();
    }

    private int videoIdx;
    @NotEmpty
    private String account;
}
