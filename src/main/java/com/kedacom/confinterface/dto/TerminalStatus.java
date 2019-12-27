package com.kedacom.confinterface.dto;

import java.util.List;

public class TerminalStatus {
    public TerminalStatus(String deviceId, String type, int status) {
        super();
        this.deviceId = deviceId;
        this.type = type;
        this.status = status;
    }

    public TerminalStatus(String deviceId, String type, int status, List<MediaResource> forwardResources, List<MediaResource> reverseResources) {
        super();
        this.deviceId = deviceId;
        this.type = type;
        this.status = status;
        this.forwardResources = forwardResources;
        this.reverseResources = reverseResources;
    }

    public TerminalStatus(String deviceId, String type, int status, List<MediaResource> forwardResources, List<MediaResource> reverseResources, int callFailureCode) {
        this.deviceId = deviceId;
        this.type = type;
        this.status = status;
        this.forwardResources = forwardResources;
        this.reverseResources = reverseResources;
        this.callFailureCode = callFailureCode;
    }

    public int getCallFailureCode() {
        return callFailureCode;
    }

    public void setCallFailureCode(int callFailureCode) {
        this.callFailureCode = callFailureCode;
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

    public List<MediaResource> getForwardResources() {
        return forwardResources;
    }

    public void setForwardResources(List<MediaResource> forwardResources) {
        this.forwardResources = forwardResources;
    }

    public List<MediaResource> getReverseResources() {
        return reverseResources;
    }

    public void setReverseResources(List<MediaResource> reverseResources) {
        this.reverseResources = reverseResources;
    }

    private String deviceId;
    private String type;
    private int status;
    private List<MediaResource> forwardResources;
    private List<MediaResource> reverseResources;

    //ÖÕ¶Ëºô½ÐÊ§°ÜÊ±µÄ´íÎóÂë
    private int callFailureCode;
}
