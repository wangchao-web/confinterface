package com.kedacom.confinterface.exchange;

public class ExchangeInfo {

    public ExchangeInfo(){
        super();
        this.deviceID = "";
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getLocalSdp() {
        return localSdp;
    }

    public String getPeerSdp() {
        return peerSdp;
    }

    public void setLocalSdp(String localSdp) {
        this.localSdp = localSdp;
    }

    public void setPeerSdp(String peerSdp) {
        this.peerSdp = peerSdp;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("resourceID:").append(resourceID)
                .append(", deviceID:").append(deviceID)
                .append(", localSdp:").append(localSdp)
                .append(", peerSdp:").append(peerSdp)
                .toString();
    }

    private String resourceID;
    private String deviceID;
    private String localSdp;
    private String peerSdp;
}
