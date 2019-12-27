package com.kedacom.confinterface.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslateCallParam  {





    public int getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }

    public String getSrcAddress() {
        return srcAddress;
    }

    public void setSrcAddress(String srcAddress) {
        this.srcAddress = srcAddress;
    }

    public String getSrcCallCode() {
        return srcCallCode;
    }

    public void setSrcCallCode(String srcCallCode) {
        this.srcCallCode = srcCallCode;
    }

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
                .append(" ,srcDeviceID : ").append(srcDeviceID)
                .append(" ,dstDeviceType : ").append(dstDeviceType)
                .append(" ,dstDeviceID:").append(dstDeviceID)
                .append(" ,srcAddress:").append(srcAddress)
                .append(" ,srcCallCode:").append(srcCallCode)
                .append(" ,srcPort:").append(srcPort)
                .append(" ,notifyURL:").append(notifyURL)
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

    @JsonProperty("SrcAddress")
    private String srcAddress;

    @JsonProperty("SrcCallCode")
    private String srcCallCode;

    @JsonProperty("SrcPort")
    private int srcPort;

    @JsonProperty("NotifyURL")
    private String notifyURL;

}
