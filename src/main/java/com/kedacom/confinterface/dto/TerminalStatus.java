package com.kedacom.confinterface.dto;

public class TerminalStatus {
    public TerminalStatus(String deviceId, int status){
        super();
        this.deviceId = deviceId;
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private String deviceId;
    private int status;
}
