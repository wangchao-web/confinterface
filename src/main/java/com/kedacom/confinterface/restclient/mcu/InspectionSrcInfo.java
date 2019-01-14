package com.kedacom.confinterface.restclient.mcu;

public class InspectionSrcInfo {

    public String getMt_id() {
        return mt_id;
    }

    public void setMt_id(String mt_id) {
        this.mt_id = mt_id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("type:").append(type).append(",mt_id:").append(mt_id).toString();
    }

    private int type;  //选看源类型: 1-终端 2-画面合成
    private String mt_id; //源终端号
}
