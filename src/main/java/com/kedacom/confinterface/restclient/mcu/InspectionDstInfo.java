package com.kedacom.confinterface.restclient.mcu;

public class InspectionDstInfo {

    public InspectionDstInfo(String mt_id){
        super();
        this.mt_id = mt_id;
    }

    public void setMt_id(String mt_id) {
        this.mt_id = mt_id;
    }

    public String getMt_id() {
        return mt_id;
    }

    @Override
    public String toString() {
        return "mt_id:"+mt_id;
    }

    private String mt_id;
}
