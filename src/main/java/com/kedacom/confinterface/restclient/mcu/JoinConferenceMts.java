package com.kedacom.confinterface.restclient.mcu;

import java.util.List;

public class JoinConferenceMts {

    public void setMts(List<JoinConferenceMtInfo> mts) {
        this.mts = mts;
    }

    public List<JoinConferenceMtInfo> getMts() {
        return mts;
    }

    @Override
    public String toString() {
        if (null == mts)
            return "";

        return mts.toString();
    }

    private List<JoinConferenceMtInfo> mts;
}
