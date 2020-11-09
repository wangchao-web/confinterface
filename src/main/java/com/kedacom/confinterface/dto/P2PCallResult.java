package com.kedacom.confinterface.dto;

public class P2PCallResult {
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public P2PVideoCallMediaCap getVidoeCodec() {
        return vidoeCodec;
    }

    public void setVidoeCodec(P2PVideoCallMediaCap vidoeCodec) {
        this.vidoeCodec = vidoeCodec;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    private String groupId;
    private P2PVideoCallMediaCap vidoeCodec;

    private String deviceName = "";
}
