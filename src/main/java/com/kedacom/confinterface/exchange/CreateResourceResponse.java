package com.kedacom.confinterface.exchange;

import com.kedacom.confinterface.dto.BaseResponseMsg;

public class CreateResourceResponse extends BaseResponseMsg {

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getSdp() {
        return sdp;
    }

    public void setSdp(String sdp) {
        this.sdp = sdp;
    }

    private String resourceID;
    private String sdp;
}
