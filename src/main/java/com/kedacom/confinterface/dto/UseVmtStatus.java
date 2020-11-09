package com.kedacom.confinterface.dto;

import java.util.List;

public class UseVmtStatus {
    public String getUseVmtType() {
        return useVmtType;
    }

    public void setUseVmtType(String useVmtType) {
        this.useVmtType = useVmtType;
    }

    public List<String> getVmtE164() {
        return vmtE164;
    }

    public void setVmtE164(List<String> vmtE164) {
        this.vmtE164 = vmtE164;
    }

    @Override
    public String toString() {
        return "UseVmtStatus{" +
                "useVmtType='" + useVmtType + '\'' +
                ", vmtE164=" + vmtE164 +
                '}';
    }

    private String useVmtType;
    private List<String> vmtE164;
}
