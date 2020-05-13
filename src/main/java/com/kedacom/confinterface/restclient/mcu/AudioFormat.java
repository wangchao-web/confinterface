package com.kedacom.confinterface.restclient.mcu;

public class AudioFormat {
    public AudioFormat(int format, int chn_num) {
        this.format = format;
        this.chn_num = chn_num;
    }

    public AudioFormat() {
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public int getChn_num() {
        return chn_num;
    }

    public void setChn_num(int chn_num) {
        this.chn_num = chn_num;
    }

    @Override
    public String toString() {
        return new StringBuffer()
                .append("format:")
                .append(format)
                .append(",chn_num:")
                .append(chn_num)
                .toString();
    }


    private int format;
    private int chn_num;

}
