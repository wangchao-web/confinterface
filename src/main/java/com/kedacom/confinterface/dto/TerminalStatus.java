package com.kedacom.confinterface.dto;

public class TerminalStatus {
    public TerminalStatus(String deviceId, String type, int status) {
        super();
        this.deviceId = deviceId;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String deviceId;
    private String type;
    private int status;
}
