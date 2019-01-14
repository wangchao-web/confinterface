package com.kedacom.confinterface.inner;

public class TransportAddress {

    public TransportAddress() {
        super();
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ip:").append(ip).append(",port:").append(port).toString();
    }

    private String ip;
    private int port;
}
