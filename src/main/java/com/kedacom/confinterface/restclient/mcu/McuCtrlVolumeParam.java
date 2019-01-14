package com.kedacom.confinterface.restclient.mcu;

public class McuCtrlVolumeParam {
    public int getVol_mode() {
        return vol_mode;
    }

    public void setVol_mode(int vol_mode) {
        this.vol_mode = vol_mode;
    }

    public int getVol_value() {
        return vol_value;
    }

    public void setVol_value(int vol_value) {
        this.vol_value = vol_value;
    }

    private int vol_mode;
    private int vol_value;
}
