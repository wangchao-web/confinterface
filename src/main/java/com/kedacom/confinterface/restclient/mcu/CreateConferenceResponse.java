package com.kedacom.confinterface.restclient.mcu;

public class CreateConferenceResponse extends McuBaseResponse {
    public void setConf_id(String conf_id) {
        this.conf_id = conf_id;
    }

    public String getConf_id() {
        return conf_id;
    }

    private String conf_id;
}
