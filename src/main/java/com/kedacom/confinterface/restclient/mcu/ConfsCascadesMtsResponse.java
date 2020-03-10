package com.kedacom.confinterface.restclient.mcu;

import java.util.List;

public class ConfsCascadesMtsResponse extends McuBaseResponse {
    public ConfsCascadesMtsResponse() {
        super();
        this.mts = null;
    }

    public List<ConfsCascadesMtsRspInfo> getMts() {
        return mts;
    }

    public void setMts(List<ConfsCascadesMtsRspInfo> mts) {
        this.mts = mts;
    }

    private List<ConfsCascadesMtsRspInfo> mts;
}
