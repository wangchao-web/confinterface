package com.kedacom.confinterface.dto;

import org.hibernate.validator.constraints.Range;

public class CtrlVolumeParam {

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public boolean checkModeValidity(){
        if (mode == 1 || mode == 2)
            return true;

        return false;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(", mode:").append(mode)
                .append(", volumeValue:").append(volume)
                .toString();
    }

    @Range(min = 1, max = 2)
    private int mode;   //1:扬声器， 2:麦克风

    @Range(min = 0, max = 35)
    private int volume;   //取值范围（0-35）
}
