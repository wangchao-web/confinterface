package com.kedacom.confinterface.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslateCallParam  {

    public String getSrcDeviceType() {
        return srcDeviceType;
    }

    public void setSrcDeviceType(String srcDeviceType) {
        this.srcDeviceType = srcDeviceType;
    }

    public String getSrcDeviceID() {
        return srcDeviceID;
    }

    public void setSrcDeviceID(String srcDeviceID) {
        this.srcDeviceID = srcDeviceID;
    }

    public String getDstDeviceType() {
        return dstDeviceType;
    }

    public void setDstDeviceType(String dstDeviceType) {
        this.dstDeviceType = dstDeviceType;
    }

    public String getDstDeviceID() {
        return dstDeviceID;
    }

    public void setDstDeviceID(String dstDeviceID) {
        this.dstDeviceID = dstDeviceID;
    }

    public String getNotifyURL() {
        return notifyURL;
    }

    public void setNotifyURL(String notifyURL) {
        this.notifyURL = notifyURL;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("srcDeviceType:").append(srcDeviceType)
                .append("srcDeviceID:").append(srcDeviceID)
                .append("dstDeviceType:").append(dstDeviceType)
                .append("dstDeviceID:").append(dstDeviceID)
                .append("notifyURL:").append(notifyURL)
                .toString();
    }

    @JsonProperty("SrcDeviceType")
    private String srcDeviceType;

    @JsonProperty("SrcDeviceID")
    private String srcDeviceID;

    @JsonProperty("DstDeviceType")
    private String dstDeviceType;

    @JsonProperty("DstDeviceID")
    private String dstDeviceID;

    @JsonProperty("NotifyURL")
    private String notifyURL;

}
