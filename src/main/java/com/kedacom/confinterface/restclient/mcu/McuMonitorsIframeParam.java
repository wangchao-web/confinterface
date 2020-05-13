package com.kedacom.confinterface.restclient.mcu;

public class McuMonitorsIframeParam {
    public McuMonitorsIframeParam(McuMonitorsDst dst) {
        this.dst = dst;
    }

    public McuMonitorsIframeParam() {
    }

    public McuMonitorsDst getDst() {
        return dst;
    }

    public void setDst(McuMonitorsDst dst) {
        this.dst = dst;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("dst:").append(dst.toString())
                .toString();
    }

    private McuMonitorsDst dst;
}
