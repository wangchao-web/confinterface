package com.kedacom.confinterface.restclient.mcu;

public class MonitorsEncrypt {
    public MonitorsEncrypt(int mode, String key) {
        this.mode = mode;
        this.key = key;
    }

    public MonitorsEncrypt() {
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private int  mode; //码流传输加密模式0-不加密；2-AES
    private String  key; //码流传输AES加密key：AES加密时用，长度最大为32
}
