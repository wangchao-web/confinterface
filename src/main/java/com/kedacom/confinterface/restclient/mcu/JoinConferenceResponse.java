package com.kedacom.confinterface.restclient.mcu;

import java.util.List;

public class JoinConferenceResponse extends McuBaseResponse {

    public JoinConferenceResponse() {
        super();
        this.mts = null;
    }

    public List<JoinConferenceRspMtInfo> getMts() {
        return mts;
    }

    public void setMts(List<JoinConferenceRspMtInfo> mts) {
        this.mts = mts;
    }

    private List<JoinConferenceRspMtInfo> mts;
}
