package com.kedacom.confinterface.sip;

import com.kedacom.confinterface.syssetting.BaseSysConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "confinterface.sip")
public class SipProtocalConfig {

    public BaseSysConfig getBaseSysConfig() {
        return baseSysConfig;
    }

    public boolean isSupportAliasCall() {
        return supportAliasCall;
    }

    public int getLocalPort() {
        return localPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setSupportAliasCall(boolean supportAliasCall) {
        this.supportAliasCall = supportAliasCall;
    }

    public void setLocalPort(int sipLocalPort) {
        this.localPort = sipLocalPort;
    }

    public void setServerIp(String sipServerIp) {
        this.serverIp = sipServerIp;
    }

    public void setServerPort(int sipServerPort) {
        this.serverPort = sipServerPort;
    }

    public void setBaseSysConfig(BaseSysConfig baseSysConfig) {
        this.baseSysConfig = baseSysConfig;
    }

    @Autowired
    protected BaseSysConfig baseSysConfig;

    private boolean supportAliasCall = false;
    private String serverIp;
    private int serverPort = 5060;
    private int localPort = 5060;
}
