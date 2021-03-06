package com.kedacom.confinterface.h323plus;

import com.kedacom.confinterface.syssetting.BaseSysConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "confinterface.h323plus")
public class H323PlusProtocalConfig {

    public BaseSysConfig getBaseSysConfig() {
        return baseSysConfig;
    }

    public void setBaseSysConfig(BaseSysConfig baseSysConfig) {
        this.baseSysConfig = baseSysConfig;
    }

    public boolean isUseGK() {
        return useGK;
    }

    public void setUseGK(boolean useGK) {
        this.useGK = useGK;
    }

    public String getGkIp() {
        return gkIp;
    }

    public void setGkIp(String gkIp) {
        this.gkIp = gkIp;
    }

    public int getGkRasPort() {
        return gkRasPort;
    }

    public void setGkRasPort(int gkRasPort) {
        this.gkRasPort = gkRasPort;
    }

    public int getGkCallPort() {
        return gkCallPort;
    }

    public void setGkCallPort(int gkCallPort) {
        this.gkCallPort = gkCallPort;
    }

    public int getLocalRasPort() {
        return localRasPort;
    }

    public void setLocalRasPort(int localRasPort) {
        this.localRasPort = localRasPort;
    }

    public int getLocalCallPort() {
        return localCallPort;
    }

    public void setLocalCallPort(int localCallPort) {
        this.localCallPort = localCallPort;
    }

    public int getRegisterGkThreadNum() {
        return registerGkThreadNum;
    }

    public void setRegisterGkThreadNum(int registerGkThreadNum) {
        this.registerGkThreadNum = registerGkThreadNum;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(",baseSysConfig:").append(baseSysConfig)
                .append(",gkIp:").append(gkIp)
                .append(",gkRasPort:").append(gkRasPort)
                .append(",gkCallPort:").append(gkCallPort)
                .append(",localRasPort:").append(localRasPort)
                .append(",localCallPort:").append(localCallPort)
                .append(",registerGkThreadNum:").append(registerGkThreadNum)
                .toString();
    }

    @Autowired
    protected BaseSysConfig baseSysConfig;

    private boolean useGK = false;
    private String gkIp = "172.16.119.66";
    private int gkRasPort = 1719;
    private int gkCallPort = 1720;
    private int localRasPort = 1729;
    private int localCallPort = 1730;
    private int registerGkThreadNum = 32;

}
