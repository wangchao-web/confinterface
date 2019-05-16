package com.kedacom.confinterface.dto;

import java.util.List;

public class VmtDetailInfo {

    public String getVmtE164() {
        return vmtE164;
    }

    public void setVmtE164(String vmtE164) {
        this.vmtE164 = vmtE164;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    private String vmtE164;
    private String groupId;
    private List<MediaResource> forwardResources;
    private List<MediaResource> reverseResources;
}
