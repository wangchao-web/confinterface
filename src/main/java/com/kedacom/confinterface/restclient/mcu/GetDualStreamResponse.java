package com.kedacom.confinterface.restclient.mcu;

public class GetDualStreamResponse extends McuBaseResponse {

    public String getMt_id() {
        return mt_id;
    }

    public void setMt_id(String mt_id) {
        this.mt_id = mt_id;
    }

    private String mt_id;
}
