package com.kedacom.confinterface.restclient.mcu;

import java.util.List;

public class LeftConferenceMts {

    public void setMts(List<TerminalId> mts) {
        this.mts = mts;
    }

    public List<TerminalId> getMts() {
        return mts;
    }

    @Override
    public String toString() {
        if (null == mts)
            return "";
        return mts.toString();
    }

    List<TerminalId> mts;
}
