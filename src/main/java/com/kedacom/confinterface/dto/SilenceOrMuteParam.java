package com.kedacom.confinterface.dto;

import org.hibernate.validator.constraints.Range;

public class SilenceOrMuteParam {

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("value:").append(value).toString();
    }

    @Range(min = 0, max = 1)
    private int value;    //静音或者哑音状态,0--停止静音或哑音,1--静音或哑音
}
