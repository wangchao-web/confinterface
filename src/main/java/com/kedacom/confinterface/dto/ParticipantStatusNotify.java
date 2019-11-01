package com.kedacom.confinterface.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ParticipantStatusNotify {
    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSilenceState() {
        return silenceState;
    }

    public void setSilenceState(int silenceState) {
        this.silenceState = silenceState;
    }

    public int getMuteState() {
        return muteState;
    }

    public void setMuteState(int muteState) {
        this.muteState = muteState;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getDualResourceID() {
        return dualResourceID;
    }

    public void setDualResourceID(String dualResourceID) {
        this.dualResourceID = dualResourceID;
    }

    @JsonProperty("GroupID")
    private String groupID;

    @JsonProperty("DeviceID")
    private String deviceID;

    @JsonProperty("Status")
    private int status;

    @JsonProperty("SilenceState")
    private int silenceState;

    @JsonProperty("MuteState")
    private int muteState;

    @JsonProperty("ResourceID")
    private String resourceID;

    @JsonProperty("dualResourceID")
    private String dualResourceID;
}
