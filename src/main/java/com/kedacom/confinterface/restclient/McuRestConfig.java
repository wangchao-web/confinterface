package com.kedacom.confinterface.restclient;

import com.kedacom.confinterface.util.ProtocalTypeEnum;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "confinterface.sys.useMcu", havingValue = "true", matchIfMissing = true)
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

    public int getConfType() {
        return confType;
    }

    public void setConfType(int confType) {
        this.confType = confType;
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
                .append(",confType:").append(confType)
                .toString();
    }

    private String softwareKey;
    private String secretKey;
    private String username;
    private String password;
    private String mcuIp;
    private int mcuRestPort;
    private String mcuRestApiVersion;
    private int bitrate = 2048;
    private String protocal = ProtocalTypeEnum.H323.getName();
    private String videoFormat;  //video_formats 4/13/25/8128主视频格式列表1-MPEG;2-H.261;3-H.263;4-H.264_HP;5-H.264_BP;6-H.265;7-H.263+;resolution*
                                 //主视频分辨率1-QCIF;2-CIF;3-4CIF;12-720P;13-1080P;14-WCIF;15-W4CIF;16-4k;
                                 //frame帧率bitrate码率
    private String audioFormat;  //音频格式列表 不填默认支持所有，空列表默认支持所有1-G.722；2-G711(ULAW)；3-G.711(ALAW)；4-G.729；
                                 //5-G.728；6-G722.1.C；7-MP3；8-G.719；9-MPEG-4 AAC LC；10-MPEG-4 AAC LD；11-MPEG-4 AAC LC(stereo)；12-MPEG-4 AAC LD(stereo)；13-OPUS；
    private int encryptedType = 0;
    private String encryptedKey;
    private int callMode = 2;
    private int callTimes = 30;
    private int callInterval = 20;
    private int confType = 0; //会议类型0-传统会议；1-端口会议；JD2000的mcu不支持端口会议,端口会议里面的每个资源都可以转码,传统会议只有发言人和主席有转码能力
}
