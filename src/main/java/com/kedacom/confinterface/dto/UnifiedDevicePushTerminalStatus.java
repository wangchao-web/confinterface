package com.kedacom.confinterface.dto;

public class UnifiedDevicePushTerminalStatus {
    public UnifiedDevicePushTerminalStatus(String callCode, String groupId, int status){
        super();
        this.callCode = callCode;
        this.groupId = groupId;
        this.status = status;
    }

    public String getCallCode() {
        return callCode;
    }

    public void setCallCode(String callCode) {
        this.callCode = callCode;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private String callCode;
    private String groupId;
    private int status;

}
