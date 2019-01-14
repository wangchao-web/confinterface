package com.kedacom.confinterface.restclient.mcu;

public class BaseChannelInfo {
    public int getChn_id() {
        return chn_id;
    }

    public void setChn_id(int chn_id) {
        this.chn_id = chn_id;
    }

    public String getChn_alias() {
        return chn_alias;
    }

    public void setChn_alias(String chn_alias) {
        this.chn_alias = chn_alias;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("chn_id:").append(chn_id).append(", chn_alias:").append(chn_alias).toString();
    }

    private int chn_id;
    private String chn_alias;
}
