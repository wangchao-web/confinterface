package com.kedacom.confinterface.exchange;

import com.kedacom.confinterface.service.ConfInterfaceInitializingService;

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

    public String getNotify_url() {
        return notify_url;
    }

    public void setNotify_url(String notify_url) {
        this.notify_url = notify_url;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(", sdp:").append(sdp)
                .append(", deviceID:").append(deviceID)
                .append(", notify_url:").append(notify_url)
                .toString();
    }

    private String deviceID;
    private String sdp;
    private String notify_url = ConfInterfaceInitializingService.notifyUrl;
}
