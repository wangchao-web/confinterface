package com.kedacom.confinterface.dto;

import javax.validation.constraints.NotNull;

public class DualStreamParam {

    public String getMtE164() {
        return mtE164;
    }

    public void setMtE164(String mtE164) {
        this.mtE164 = mtE164;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("mtE164:").append(mtE164).toString();
    }

    @NotNull
    private String mtE164;
}
