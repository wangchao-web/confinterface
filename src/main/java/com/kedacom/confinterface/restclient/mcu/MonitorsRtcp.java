package com.kedacom.confinterface.restclient.mcu;

public class MonitorsRtcp {
    public MonitorsRtcp(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public MonitorsRtcp() {
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private String  ip; //rtcp地址
    private int  port; //rtcp端口号
}
