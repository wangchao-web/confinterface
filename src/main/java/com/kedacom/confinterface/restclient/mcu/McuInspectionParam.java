package com.kedacom.confinterface.restclient.mcu;

public class McuInspectionParam {

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public InspectionSrcInfo getSrc() {
        return src;
    }

    public void setSrc(InspectionSrcInfo src) {
        this.src = src;
    }

    public InspectionDstInfo getDst() {
        return dst;
    }

    public void setDst(InspectionDstInfo dst) {
        this.dst = dst;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("src{").append(src).append("},dst{").append(dst).append("},mode:").append(mode).toString();
    }

    private Integer mode;  // 1:视频, 2:音频
    private InspectionSrcInfo src;
    private InspectionDstInfo dst;
}
