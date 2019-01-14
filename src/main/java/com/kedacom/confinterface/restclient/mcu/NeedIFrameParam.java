package com.kedacom.confinterface.restclient.mcu;

import com.kedacom.confinterface.inner.TransportAddress;

public class NeedIFrameParam {
    public TransportAddress getDst() {
        return dst;
    }

    public void setDst(TransportAddress dst) {
        this.dst = dst;
    }

    private TransportAddress dst;
}
