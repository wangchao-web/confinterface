package com.kedacom.confinterface.inner;

import com.kedacom.confinterface.restclient.mcu.InspectionStatusEnum;

public class InspectedParam {

    public InspectedParam(){
        super();
        this.status = InspectionStatusEnum.UNKNOWN;
    }

    public boolean isVmt() {
        return vmt;
    }

    public void setVmt(boolean vmt) {
        this.vmt = vmt;
    }

    public InspectionStatusEnum getStatus() {
        return status;
    }

    public void setStatus(InspectionStatusEnum status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("vmt:").append(vmt).append(", status:").append(status.getName()).toString();
    }

    private boolean vmt;   //选看目的是否是vmt
    private InspectionStatusEnum status;
}
