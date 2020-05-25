package com.kedacom.confinterface.syssetting;

import com.kedacom.confinterface.util.AudioCap;
import com.kedacom.confinterface.util.VideoCap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Component
@ConfigurationProperties(prefix = "confinterface.sys")
public class BaseSysConfig {

    public BaseSysConfig() {
        super();
        this.videoCapList = null;
        this.audioCapList = null;
        this.proxyMTs = null;
        this.mapProxyMTs = null;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public void setMediaSrvIp(String mediaSrvIp) {
        this.mediaSrvIp = mediaSrvIp;
    }

    public void setMediaSrvPort(int mediaSrvPort) {
        this.mediaSrvPort = mediaSrvPort;
    }

    public String getScheduleSrvHttpAddr() {
        return scheduleSrvHttpAddr;
    }

    public void setScheduleSrvHttpAddr(String scheduleSrvHttpAddr) {
        this.scheduleSrvHttpAddr = scheduleSrvHttpAddr;
    }

    public String getMediaSrvIp() {
        return mediaSrvIp;
    }

    public int getMediaSrvPort() {
        return mediaSrvPort;
    }

    public String getProtocalType() {
        return protocalType;
    }

    public void setProtocalType(String protocalType) {
        this.protocalType = protocalType;
    }

    public String getMemDBType() {
        return memDBType;
    }

    public void setMemDBType(String memDBType) {
        this.memDBType = memDBType;
    }

    public boolean isUseMcu() {
        return useMcu;
    }

    public void setUseMcu(boolean useMcu) {
        this.useMcu = useMcu;
    }

    public int getMaxVmts() {
        return maxVmts;
    }

    public void setMaxVmts(int maxVmts) {
        this.maxVmts = maxVmts;
    }

    public String getVmtNamePrefix() {
        return vmtNamePrefix;
    }

    public void setVmtNamePrefix(String vmtNamePrefix) {
        this.vmtNamePrefix = vmtNamePrefix;
    }

    public String getE164Start() {
        return e164Start;
    }

    public void setE164Start(String e164Start) {
        this.e164Start = e164Start;
    }

    public String getProxyMTs(){ return proxyMTs; }

    public void setProxyMTs(String proxyMTs) { this.proxyMTs = proxyMTs; }

    public ConcurrentHashMap<String, String> getMapProxyMTs(){
        if (null == proxyMTs)
            return null;

        synchronized (this){
            mapProxyMTs = new ConcurrentHashMap<>();

            String[] mtE164s = proxyMTs.split(",");
            for(String mtE164 : mtE164s){
                String[] bindingRelation = mtE164.split(":");
                mapProxyMTs.put(bindingRelation[0], bindingRelation[1]);
            }

            return mapProxyMTs;
        }
    }

    public String getCdeviceManageSrvAddr() {
        return cdeviceManageSrvAddr;
    }

    public void setCdeviceManageSrvAddr(String cdeviceManageSrvAddr) {
        this.cdeviceManageSrvAddr = cdeviceManageSrvAddr;
    }

    public void setVideoCapSet(String videoCapSet) {
        this.videoCapSet = videoCapSet;
    }

    public String getVideoCapSet() {
        return videoCapSet;
    }

    public void setAudioCapSet(String audioCapSet) {
        this.audioCapSet = audioCapSet;
    }

    public String getAudioCapSet() {
        return audioCapSet;
    }

    public List<VideoCap> getVideoCapSetList() {
        if (null != videoCapList) {
            return videoCapList;
        }

        synchronized (BaseSysConfig.class) {
            videoCapList = new ArrayList<>();

            String[] videoCaps = videoCapSet.split(",");
            for (String videoCap : videoCaps) {
                String[] videoParam = videoCap.split("/", 5);

                VideoCap videoCapResult = new VideoCap();
                videoCapResult.setMediaType(Integer.valueOf(videoParam[0]));
                videoCapResult.setResolution(Integer.valueOf(videoParam[1]));
                videoCapResult.setFrameRate(Integer.valueOf(videoParam[2]));
                videoCapResult.setBitRateType(Integer.valueOf(videoParam[3]));
                videoCapResult.setBitRate(Integer.valueOf(videoParam[4]));

                videoCapList.add(videoCapResult);
            }
            return videoCapList;
        }
    }

    public List<AudioCap> getAudioCapSetList() {
        if (null != audioCapList) {
            return audioCapList;
        }

        synchronized (BaseSysConfig.class) {
            audioCapList = new ArrayList<>();

            String[] audioCaps = audioCapSet.split(",");
            for (String audioCap : audioCaps) {
                String[] audioParam = audioCap.split("/", 4);

                AudioCap audioCapResult = new AudioCap();
                audioCapResult.setMediaType(Integer.valueOf(audioParam[0]));
                audioCapResult.setBitRate(Integer.valueOf(audioParam[1]));
                audioCapResult.setSampleRate(Integer.valueOf(audioParam[2]));
                audioCapResult.setChannelNum(Integer.valueOf(audioParam[3]));

                audioCapList.add(audioCapResult);
            }
            return audioCapList;
        }
    }

    public String getPushServiceType() {
        return pushServiceType;
    }

    public void setPushServiceType(String pushServiceType) {
        this.pushServiceType = pushServiceType;
    }

    public int getLogFileSize() {
        return logFileSize;
    }

    public void setLogFileSize(int logFileSize) {
        this.logFileSize = logFileSize;
    }

    public int getLogFileNum() {
        return logFileNum;
    }

    public void setLogFileNum(int logFileNum) {
        this.logFileNum = logFileNum;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public static Boolean getIsExternalDocking() {
        return isExternalDocking;
    }

    @Value("${confinterface.sys.isExternalDocking:false}")
    public  void setIsExternalDocking(Boolean isExternalDocking) {
        this.isExternalDocking = isExternalDocking;
    }




    @Override
    public String toString() {
        return new StringBuffer()
                .append("mcuMode:")
                .append(mcuMode)
                .append(",localIp:")
                .append(localIp)
                .append(", mediaSrvIp:")
                .append(mediaSrvIp)
                .append(", mediaSrvPort:")
                .append(mediaSrvPort)
                .append(", protocalType:")
                .append(protocalType)
                .append(", memDBType:")
                .append(memDBType)
                .append(", useMcu:")
                .append(useMcu)
                .append(", maxVmts:")
                .append(maxVmts)
                .append(", vmtNamePrefix:")
                .append(vmtNamePrefix)
                .append(", e164Start:")
                .append(e164Start)
                .append(", pushServiceType:")
                .append(pushServiceType)
                .append(", videoCaps:")
                .append(videoCapSet)
                .append(", audioCaps:")
                .append(audioCapSet)
                .toString();
    }

    public String getMcuMode() {
        return mcuMode;
    }

    public void setMcuMode(String mcuMode) {
        this.mcuMode = mcuMode;
    }

    public Boolean getCalled() {
        return isCalled;
    }

    public void setCalled(Boolean called) {
        isCalled = called;
    }

    private String mcuMode = "mcu";
    private String localIp = "172.16.64.25";
    private String mediaSrvIp = "172.16.64.25";
    private int mediaSrvPort = 8080;
    private String scheduleSrvHttpAddr = "172.16.64.25:8085";
    private String protocalType = "h323";
    private String memDBType = "redis";
    private boolean useMcu = true;
    private int maxVmts = 3;
    private String vmtNamePrefix = "confInterface_";
    private String e164Start; //"1234560100000";   虚拟终端的E164号起始编号
    private String proxyMTs;   //存储虚拟终端与实体终端的E164号的对应关系,形式为key:value，key:value...
    private ConcurrentHashMap<String, String> mapProxyMTs;
    private String cdeviceManageSrvAddr = "dev.ctsp.kedacom.com";
    private String pushServiceType = "mediaSchedule"; // ctsp向统一设备推送状态 ; mediaSchedule 向媒体调度推送状态
    /*编码格式/分辨率/帧率/码率类型/码率
     * 编码格式：1：MPEG-4，2：H264， 3：SVAC，4：3GP
     * 分辨率：1：QCIF，2：CIF，3：4CIF，4：D1，5：720P，6：1080P
     * 帧率：0-99之间的整数
     * 码率类型：1：固定码率，2：可变码率
     * 码率：0-100000之间的整数，1表示1kbps
     * */
    private String videoCapSet = "2/6/30/1/4096";
    /*编码格式/码率/采样率
     * 编码格式：1：G711， 2：G.723.1, 3: G729, 4: G.722.1
     * 码率：1：5.3kbps(G.723.1中使用)，2：6.3kbps(G.723.1中使用), 3:8kbps(G729中使用)
     *      4：16kbps(G.722.1中使用)， 5：24kbps(G.722.1中使用), 6: 32kbps(G.722.1中使用),
     *      7: 48kbps(G.722.1中使用), 8: 64kbps(G711中使用)
     * 采样率：1：8kHz(G711/G.723.1/G729中使用), 2: 14kHz(G.722.1中使用),
     *        3: 16kHz(G.722.1中使用), 4: 32kHz(G.722.1中使用)
     * 通道数：>=1的整型值
     * */
    private String audioCapSet = "1/8/1/2";

    private int logFileSize = 1048576;

    private int logFileNum = 2;

    private String logPath = "/usr/";

    private List<VideoCap> videoCapList;
    private List<AudioCap> audioCapList;

    private Boolean isCalled = false;  //是否被叫

    //@Value("${confinterface.sys.isExternalDocking}")
    public static Boolean isExternalDocking = false;  //适应外厂商paload
}
