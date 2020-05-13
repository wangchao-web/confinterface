package com.kedacom.confinterface.dto;

import java.io.Serializable;

public class MonitorsMember implements Serializable {
    public MonitorsMember(int mode, int type, String id, String e164, String dstIp, int port) {
        this.mode = mode;
        this.type = type;
        this.id = id;
        this.E164 = e164;
        this.dstIp = dstIp;
        this.port = port;
    }

    public MonitorsMember() {
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getE164() {
        return E164;
    }

    public void setE164(String e164) {
        E164 = e164;
    }

    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMtId() {
        return mtId;
    }

    public void setMtId(String mtId) {
        this.mtId = mtId;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("mode:").append(mode)
                .append(", type:").append(type)
                .append(", id:").append(id)
                .append(", E164:").append(E164)
                .append(", dstIp:").append(dstIp)
                .append(", port:").append(port)
                .append(", mtId:").append(mtId)
                .toString();
    }

    private int mode;      //资源类型，"0-video","1-audio"
    private int type;   //监控类型 1-终端；2-画面合成；3-混音；
    private String id;      //资源号，由流媒体返回
    private String E164;   //终端E164,
    private String dstIp;    //监控终端发流的目的ip
    private int port;
    private String mtId = "";  //终端所对应的mtId, 2-画面合成；3-混音为"",
}
