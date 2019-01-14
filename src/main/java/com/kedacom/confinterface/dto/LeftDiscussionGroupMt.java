package com.kedacom.confinterface.dto;

import javax.validation.constraints.NotBlank;

public class LeftDiscussionGroupMt {
    public String getMtE164() {
        return mtE164;
    }

    public void setMtE164(String mtE164) {
        this.mtE164 = mtE164;
    }

    public boolean isStopInspection() {
        return stopInspection;
    }

    public void setStopInspection(boolean stopInspection) {
        this.stopInspection = stopInspection;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("mtE164:").append(mtE164).append(", stopInspection:").append(stopInspection).toString();
    }

    @NotBlank
    private String mtE164;
    private boolean stopInspection;
}
