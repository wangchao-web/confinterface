package com.kedacom.confinterface.restclient.mcu;

import java.util.List;

public class GetCascadesMtResponse extends McuBaseResponse {

    public void setMts(List<CascadeTerminalInfo> mts) {
        this.mts = mts;
    }

    public List<CascadeTerminalInfo> getMts() {
        return mts;
    }

    private List<CascadeTerminalInfo> mts;
}
