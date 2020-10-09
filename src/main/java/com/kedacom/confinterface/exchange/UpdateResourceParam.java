package com.kedacom.confinterface.exchange;

public class UpdateResourceParam extends CreateResourceParam {

    public UpdateResourceParam(String resourceID) {
        super();
        this.resourceID = resourceID;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("resourceID:").append(resourceID).
                append(", sdp:").append(super.getSdp())
                .append(", deviceID:").append(super.getDeviceID())
                .toString();
    }

    private String resourceID;
}
