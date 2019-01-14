package com.kedacom.confinterface.sip;

import com.kedacom.confinterface.syssetting.BaseSysConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "confinterface.sip")
public class SipProtocalConfig {

    public BaseSysConfig getBaseSysConfig() {
        return baseSysConfig;
    }

    public int getSipLocalPort() {
        return sipLocalPort;
    }

    public int getSipServerPort() {
        return sipServerPort;
    }

    public String getSipServerIp() {
        return sipServerIp;
    }

    public void setSipLocalPort(int sipLocalPort) {
        this.sipLocalPort = sipLocalPort;
    }

    public void setSipServerIp(String sipServerIp) {
        this.sipServerIp = sipServerIp;
    }

    public void setSipServerPort(int sipServerPort) {
        this.sipServerPort = sipServerPort;
    }

    public void setBaseSysConfig(BaseSysConfig baseSysConfig) {
        this.baseSysConfig = baseSysConfig;
    }

    @Autowired
    protected BaseSysConfig baseSysConfig;

    private String sipServerIp;
    private int sipServerPort;
    private int sipLocalPort;
}
