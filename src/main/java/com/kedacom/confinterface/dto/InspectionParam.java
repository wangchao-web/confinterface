package com.kedacom.confinterface.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class InspectionParam {

    public void setSrcMtE164(String srcMtE164) {
        this.srcMtE164 = srcMtE164;
    }

    public String getSrcMtE164() {
        return srcMtE164;
    }

    public String getDstMtE164() {
        return dstMtE164;
    }

    public void setDstMtE164(String dstMtE164) {
        this.dstMtE164 = dstMtE164;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("srcMtE164:")
                .append(srcMtE164)
                .append(", dstMtE164:")
                .append(dstMtE164)
                .append(", mode:")
                .append(mode)
                .toString();
    }

    @NotNull
    private String srcMtE164;
    @NotNull
    private String dstMtE164;
    @NotBlank
    private String mode;
}
