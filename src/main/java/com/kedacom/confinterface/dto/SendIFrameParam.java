package com.kedacom.confinterface.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class SendIFrameParam {
    public String getMtE164() {
        return mtE164;
    }

    public void setMtE164(String mtE164) {
        this.mtE164 = mtE164;
    }

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("mtE164:").append(mtE164).append(", resourceIds:").append(resourceIds).toString();
    }

    @NotBlank
    private String mtE164;

    @NotEmpty
    private List<String> resourceIds;
}
