package com.kedacom.confinterface.dto;

public class P2PCallResult {
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public P2PCallMediaCap getVidoeCodec() {
        return vidoeCodec;
    }

    public void setVidoeCodec(P2PCallMediaCap vidoeCodec) {
        this.vidoeCodec = vidoeCodec;
    }

    private String groupId;
    private P2PCallMediaCap vidoeCodec;
}
