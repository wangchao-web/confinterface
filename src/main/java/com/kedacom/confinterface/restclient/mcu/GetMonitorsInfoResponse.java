package com.kedacom.confinterface.restclient.mcu;


public class GetMonitorsInfoResponse extends McuBaseResponse {

    public GetMonitorsInfoResponse(int mode, GetMonitorsSrc src, McuMonitorsDst dst) {
        this.mode = mode;
        this.src = src;
        this.dst = dst;
    }

    public GetMonitorsInfoResponse() {
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public GetMonitorsSrc getSrc() {
        return src;
    }

    public void setSrc(GetMonitorsSrc src) {
        this.src = src;
    }

    public McuMonitorsDst getDst() {
        return dst;
    }

    public void setDst(McuMonitorsDst dst) {
        this.dst = dst;
    }

    private int mode; //监控模式0-视频；1-音频；
    private GetMonitorsSrc src;
    private McuMonitorsDst dst;
}
