package com.kedacom.confinterface.restclient.mcu;

public class TerminalInfo extends CascadeTerminalInfo {

    public int getInspection() {
        return inspection;
    }

    public void setInspection(int inspection) {
        this.inspection = inspection;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public int getCall_mode() {
        return call_mode;
    }

    public void setCall_mode(int call_mode) {
        this.call_mode = call_mode;
    }

    private int inspection;
    private int protocol;
    private int call_mode;
}
