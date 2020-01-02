package com.kedacom.confinterface.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class SendIFrameParam {
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("resourceId:").append(resourceId).toString();
    }

    @NotBlank
    private String resourceId;
}
