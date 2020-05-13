package com.kedacom.confinterface.restclient.mcu;

public class MonitorsPayloadFormats {
    public MonitorsPayloadFormats(String real_payload, String active_payload) {
        this.real_payload = real_payload;
        this.active_payload = active_payload;
    }

    public MonitorsPayloadFormats() {
    }

    public String getReal_payload() {
        return real_payload;
    }

    public void setReal_payload(String real_payload) {
        this.real_payload = real_payload;
    }

    public String getActive_payload() {
        return active_payload;
    }

    public void setActive_payload(String active_payload) {
        this.active_payload = active_payload;
    }

    private String  real_payload; //原载荷
    private String active_payload; //动态载荷

}
