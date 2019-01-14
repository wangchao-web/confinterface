package com.kedacom.confinterface.restclient.mcu;

public class OnlineMt {
    public String getMt_id() {
        return mt_id;
    }

    public void setMt_id(String mt_id) {
        this.mt_id = mt_id;
    }

    public int getForced_call() {
        return forced_call;
    }

    public void setForced_call(int forced_call) {
        this.forced_call = forced_call;
    }

    private String mt_id;
    private int forced_call;    //是否强呼，0-不强呼，1-强呼
}
