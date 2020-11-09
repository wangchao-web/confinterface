package com.kedacom.confinterface.dao;

public class Connstatus {
    public Connstatus() {
        this.ip = "";
        this.status = "";
        this.port = "";
        this.componentname = "";
        this.username = null;
        this.password = null;
    }

    public Connstatus(String ip, String status, String port, String componentname, String username, String password) {
        this.ip = ip;
        this.status = status;
        this.port = port;
        this.componentname = componentname;
        this.username = username;
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getComponentname() {
        return componentname;
    }

    public void setComponentname(String componentname) {
        this.componentname = componentname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String ip;
    private String status;
    private String port;
    private String componentname;
    private String username;
    private String password;
}
