package com.kedacom.confinterface.restclient.mcu;

public class McuMonitorsDst {
    public McuMonitorsDst(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public McuMonitorsDst() {
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

    @Override
    public String toString() {
        return new StringBuilder().append("ip:").append(ip)
                .append(", port:").append(port)
                .toString();
    }

    private String ip;
    private int port;
}
