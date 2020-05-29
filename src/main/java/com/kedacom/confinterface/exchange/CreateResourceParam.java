package com.kedacom.confinterface.exchange;

public class CreateResourceParam {
    public CreateResourceParam(){
        super();
    }

    public void setSdp(String sdp) {
        this.sdp = sdp;
    }

    public String getSdp() {
        return sdp;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(", sdp:").append(sdp)
                .append(", deviceID:").append(deviceID).toString().toString();
    }

    private String deviceID;
    private String sdp;
}
