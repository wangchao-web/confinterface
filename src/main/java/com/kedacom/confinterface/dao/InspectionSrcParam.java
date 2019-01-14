package com.kedacom.confinterface.dao;

import java.io.Serializable;

public class InspectionSrcParam implements Serializable {

    public String getMtE164() {
        return mtE164;
    }

    public void setMtE164(String mtE164) {
        this.mtE164 = mtE164;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("mtE164:").append(mtE164).append(", mode:").append(mode).toString();
    }

    private String mtE164;
    private String mode;
}
