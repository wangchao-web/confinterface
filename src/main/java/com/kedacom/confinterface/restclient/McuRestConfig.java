package com.kedacom.confinterface.restclient;

import com.kedacom.confinterface.util.ProtocalTypeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "confinterface.mcu.rest")
public class McuRestConfig {

    public String getSoftwareKey() {
        return softwareKey;
    }

    public void setSoftwareKey(String softwareKey) {
        this.softwareKey = softwareKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
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

    public String getMcuIp() {
        return mcuIp;
    }

    public void setMcuIp(String mcuIp) {
        this.mcuIp = mcuIp;
    }

    public int getMcuRestPort() {
        return mcuRestPort;
    }

    public void setMcuRestPort(int mcuRestPort) {
        this.mcuRestPort = mcuRestPort;
    }

    public String getMcuRestApiVersion() {
        return mcuRestApiVersion;
    }

    public void setMcuRestApiVersion(String mcuRestApiVersion) {
        this.mcuRestApiVersion = mcuRestApiVersion;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getProtocal() {
        return protocal;
    }

    public void setProtocal(String protocal) {
        this.protocal = protocal;
    }

    public int getProtocalCode() {
        if (protocal.equals(ProtocalTypeEnum.H323.getName())) {
            return ProtocalTypeEnum.H323.getCode();
        } else if (protocal.equals(ProtocalTypeEnum.SIP.getName())) {
            return ProtocalTypeEnum.SIP.getCode();
        }

        return ProtocalTypeEnum.H323.getCode();
    }

    public String getVideoFormat() {
        return videoFormat;
    }

    public void setVideoFormat(String videoFormat) {
        this.videoFormat = videoFormat;
    }

    public String getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(String audioFormat) {
        this.audioFormat = audioFormat;
    }

    public int getEncryptedType() {
        return encryptedType;
    }

    public void setEncryptedType(int encryptedType) {
        this.encryptedType = encryptedType;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public int getCallMode() {
        return callMode;
    }

    public int getCallInterval() {
        return callInterval;
    }

    public int getCallTimes() {
        return callTimes;
    }

    public void setCallMode(int callMode) {
        this.callMode = callMode;
    }

    public void setCallInterval(int callInterval) {
        this.callInterval = callInterval;
    }

    public void setCallTimes(int callTimes) {
        this.callTimes = callTimes;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("softwareKey:").append(softwareKey)
                .append(",secretKey:").append(secretKey)
                .append(",username:").append(username)
                .append(",password:").append(password)
                .append(",mcuIp:").append(mcuIp)
                .append(",mcuRestPort:").append(mcuRestPort)
                .append(",mcuRestApiVersion:").append(mcuRestApiVersion)
                .append(",bitrate:").append(bitrate)
                .append(",protocal:").append(protocal)
                .append(",videoFormat:").append(videoFormat)
                .append(", audioFormat:").append(audioFormat)
                .append(",encryptedType:").append(encryptedType)
                .append(",encryptedKey:").append(encryptedKey)
                .append(",callMode:").append(callMode)
                .append(",callTimes:").append(callTimes)
                .append(",callInterval:").append(callInterval)
                .toString();
    }

    private String softwareKey;
    private String secretKey;
    private String username;
    private String password;
    private String mcuIp;
    private int mcuRestPort;
    private String mcuRestApiVersion;
    private int bitrate;
    private String protocal = ProtocalTypeEnum.H323.getName();
    private String videoFormat;
    private String audioFormat;
    private int encryptedType = 0;
    private String encryptedKey;
    private int callMode = 2;
    private int callTimes = 30;
    private int callInterval = 20;
}
