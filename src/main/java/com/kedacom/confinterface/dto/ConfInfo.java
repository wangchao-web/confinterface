package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.restclient.mcu.Creator;
import com.kedacom.confinterface.restclient.mcu.VideoFormat;

import java.util.List;

public class ConfInfo  {
    
    public ConfInfo(String name, String meetingRoomName, String confId, int confLevel, int confType, String startTime, String endTime, int duration, int bitrate, int closedConf, int safeConf, int encryptedType, int mute, int muteFilter,  int silence, int videoQuality, String encryptedKey, int dualMode, int publicConf, int autoEnd, int preoccupyResource, int maxJoinMt, int forceBroadcast, int fecMode, int voiceActivityDetection, int vacinterval, int callTimes, int callInterval, int callMode, int cascadeMode, int cascadeReturn, int cascadeReturnPara, int vmpEnable, int mixEnable, int pollEnable, int needPassword, int oneReforming, int doubleflow, String platformId) {
        this.name = name;
        this.meetingRoomName = meetingRoomName;
        this.confId = confId;
        this.confLevel = confLevel;
        this.confType = confType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.bitrate = bitrate;
        this.closedConf = closedConf;
        this.safeConf = safeConf;
        this.encryptedType = encryptedType;
        this.mute = mute;
        this.muteFilter = muteFilter;
        this.silence = silence;
        this.videoQuality = videoQuality;
        this.encryptedKey = encryptedKey;
        this.dualMode = dualMode;
        this.publicConf = publicConf;
        this.autoEnd = autoEnd;
        this.preoccupyResource = preoccupyResource;
        this.maxJoinMt = maxJoinMt;
        this.forceBroadcast = forceBroadcast;
        this.fecMode = fecMode;
        this.voiceActivityDetection = voiceActivityDetection;
        this.vacinterval = vacinterval;
        this.callTimes = callTimes;
        this.callInterval = callInterval;
        this.callMode = callMode;
        this.cascadeMode = cascadeMode;
        this.cascadeReturn = cascadeReturn;
        this.cascadeReturnPara = cascadeReturnPara;
        this.vmpEnable = vmpEnable;
        this.mixEnable = mixEnable;
        this.pollEnable = pollEnable;
        this.needPassword = needPassword;
        this.oneReforming = oneReforming;
        this.doubleflow = doubleflow;
        this.platformId = platformId;
    }

    public ConfInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeetingRoomName() {
        return meetingRoomName;
    }

    public void setMeetingRoomName(String meetingRoomName) {
        this.meetingRoomName = meetingRoomName;
    }

    public String getConfId() {
        return confId;
    }

    public void setConfId(String confId) {
        this.confId = confId;
    }

    public int getConfLevel() {
        return confLevel;
    }

    public void setConfLevel(int confLevel) {
        this.confLevel = confLevel;
    }

    public int getConfType() {
        return confType;
    }

    public void setConfType(int confType) {
        this.confType = confType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getClosedConf() {
        return closedConf;
    }

    public void setClosedConf(int closedConf) {
        this.closedConf = closedConf;
    }

    public int getSafeConf() {
        return safeConf;
    }

    public void setSafeConf(int safeConf) {
        this.safeConf = safeConf;
    }

    public int getEncryptedType() {
        return encryptedType;
    }

    public void setEncryptedType(int encryptedType) {
        this.encryptedType = encryptedType;
    }

    public int getMute() {
        return mute;
    }

    public void setMute(int mute) {
        this.mute = mute;
    }

    public int getMuteFilter() {
        return muteFilter;
    }

    public void setMuteFilter(int muteFilter) {
        this.muteFilter = muteFilter;
    }


    public int getSilence() {
        return silence;
    }

    public void setSilence(int silence) {
        this.silence = silence;
    }

    public int getVideoQuality() {
        return videoQuality;
    }

    public void setVideoQuality(int videoQuality) {
        this.videoQuality = videoQuality;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public int getDualMode() {
        return dualMode;
    }

    public void setDualMode(int dualMode) {
        this.dualMode = dualMode;
    }

    public int getPublicConf() {
        return publicConf;
    }

    public void setPublicConf(int publicConf) {
        this.publicConf = publicConf;
    }

    public int getAutoEnd() {
        return autoEnd;
    }

    public void setAutoEnd(int autoEnd) {
        this.autoEnd = autoEnd;
    }

    public int getPreoccupyResource() {
        return preoccupyResource;
    }

    public void setPreoccupyResource(int preoccupyResource) {
        this.preoccupyResource = preoccupyResource;
    }

    public int getMaxJoinMt() {
        return maxJoinMt;
    }

    public void setMaxJoinMt(int maxJoinMt) {
        this.maxJoinMt = maxJoinMt;
    }

    public int getForceBroadcast() {
        return forceBroadcast;
    }

    public void setForceBroadcast(int forceBroadcast) {
        this.forceBroadcast = forceBroadcast;
    }

    public int getFecMode() {
        return fecMode;
    }

    public void setFecMode(int fecMode) {
        this.fecMode = fecMode;
    }

    public int getVoiceActivityDetection() {
        return voiceActivityDetection;
    }

    public void setVoiceActivityDetection(int voiceActivityDetection) {
        this.voiceActivityDetection = voiceActivityDetection;
    }

    public int getVacinterval() {
        return vacinterval;
    }

    public void setVacinterval(int vacinterval) {
        this.vacinterval = vacinterval;
    }

    public int getCallTimes() {
        return callTimes;
    }

    public void setCallTimes(int callTimes) {
        this.callTimes = callTimes;
    }

    public int getCallInterval() {
        return callInterval;
    }

    public void setCallInterval(int callInterval) {
        this.callInterval = callInterval;
    }

    public int getCallMode() {
        return callMode;
    }

    public void setCallMode(int callMode) {
        this.callMode = callMode;
    }

    public int getCascadeMode() {
        return cascadeMode;
    }

    public void setCascadeMode(int cascadeMode) {
        this.cascadeMode = cascadeMode;
    }

    public int getCascadeReturn() {
        return cascadeReturn;
    }

    public void setCascadeReturn(int cascadeReturn) {
        this.cascadeReturn = cascadeReturn;
    }

    public int getCascadeReturnPara() {
        return cascadeReturnPara;
    }

    public void setCascadeReturnPara(int cascadeReturnPara) {
        this.cascadeReturnPara = cascadeReturnPara;
    }

    public int getVmpEnable() {
        return vmpEnable;
    }

    public void setVmpEnable(int vmpEnable) {
        this.vmpEnable = vmpEnable;
    }

    public int getMixEnable() {
        return mixEnable;
    }

    public void setMixEnable(int mixEnable) {
        this.mixEnable = mixEnable;
    }

    public int getPollEnable() {
        return pollEnable;
    }

    public void setPollEnable(int pollEnable) {
        this.pollEnable = pollEnable;
    }

    public int getNeedPassword() {
        return needPassword;
    }

    public void setNeedPassword(int needPassword) {
        this.needPassword = needPassword;
    }

    public int getOneReforming() {
        return oneReforming;
    }

    public void setOneReforming(int oneReforming) {
        this.oneReforming = oneReforming;
    }

    public int getDoubleflow() {
        return doubleflow;
    }

    public void setDoubleflow(int doubleflow) {
        this.doubleflow = doubleflow;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public List<VideoFormat> getVideoFormats() {
        return videoFormats;
    }

    public void setVideoFormats(List<VideoFormat> videoFormats) {
        this.videoFormats = videoFormats;
    }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("name:").append(name)
                .append(", meetingRoomName:").append(meetingRoomName)
                .append(", confId:").append(confId)
                .append(", confLevel:").append(confLevel)
                .append(", confType:").append(confType)
                .append(", bitrate:").append(bitrate)
                .append(", startTime:").append(startTime)
                .append(", endTime:").append(endTime)
                .append(", duration:").append(duration)
                .append(", bitrate:").append(bitrate)
                .append(", closedConf:").append(closedConf)
                .append(", safeConf:").append(safeConf)
                .append(", encryptedType:").append(encryptedType)
                .append(", mute:").append(mute)
                .append(", muteFilter:").append(muteFilter)
                .append(", silence:").append(silence)
                .append(", videoQuality:").append(videoQuality)
                .append(", encryptedKey:").append(encryptedKey)
                .append(", dualMode:").append(dualMode)
                .append(", publicConf:").append(publicConf)
                .append(", preoccupyResource:").append(preoccupyResource)
                .append(", autoEnd:").append(autoEnd)
                .append(", maxJoinMt:").append(maxJoinMt)
                .append(", forceBroadcast:").append(forceBroadcast)
                .append(", fecMode:").append(fecMode)
                .append(", voiceActivityDetection:").append(voiceActivityDetection)
                .append(", vacinterval:").append(vacinterval)
                .append(", callTimes:").append(callTimes)
                .append(", callInterval:").append(callInterval)
                .append(", callMode:").append(callMode)
                .append(", cascadeMode:").append(cascadeMode)
                .append(", cascadeReturn:").append(cascadeReturn)
                .append(", cascadeReturnPara:").append(cascadeReturnPara)
                .append(", vmpEnable:").append(vmpEnable)
                .append(", mixEnable:").append(mixEnable)
                .append(", pollEnable:").append(pollEnable)
                .append(", needPassword:").append(needPassword)
                .append(", oneReforming:").append(oneReforming)
                .append(", doubleflow:").append(doubleflow)
                .append(", platformId:").append(platformId)
                .append(", videoFormats:").append(videoFormats.toString())
                .append(", creator:").append(creator.toString())
                .toString();
    }

    private String name;   //会议名称 最大字符长度：64个字节
    private String meetingRoomName; //虚拟会议室名称，当返回为空字符串时说明不是召开的虚拟会议室
    private String confId; //会议号 最大字符长度：48个字节；
    private int confLevel; //会议级别：1-16, 值越小, 级别越高(用于抢呼已在其他会议里的终端)
    private int confType; //会议类型0-传统；1-端口；
    private String startTime ; //会议开始时间（ISO8601:2000格式表示
    private String endTime; //会议结束时间（ISO8601:2000格式表示）
    private int duration ; //会议时长 0为永久会议
    private int bitrate; //会议码率
    private int closedConf;  //会议免打扰0-关闭；1-开启；
    private int safeConf;  //会议安全0-公开会议；1-隐藏会议；
    private int encryptedType; //传输加密类型0-不加密；2-AES加密；
    private int mute; //初始化哑音0-否；1-是；
    private int muteFilter;  //全场哑音例外，参数为1时，若执行全场哑音操作，主席和发言人不会被哑音，若执行单个哑音操作时可以被哑音0-否；1-是；
    private int silence;  //初始化静音0-否；1-是；
    private int videoQuality;  //视频质量0-质量优先；1-速度优先；
    private String encryptedKey;  //传输加密AES加密密钥 最大字符长度：16字节
    private int dualMode;  //双流权限0-发言会场；1-任意会场；2-指定会场；
    private int publicConf;  //是否公共会议室0-否；1-是；
    private int autoEnd;  //自动结会(少于两个终端时自动结会)0-不自动结束；1-自动结束；
    private int preoccupyResource;//预占资源(创会时就预占音视频适配器)0-不预占；1-预占；
    private int maxJoinMt; //最大与会终端数8-小型8方会议；32-32方会议；64-64方会议；192-大型192方会议；
    private int forceBroadcast; //是否强制广播0-不强制广播；1-强制广播；
    private int fecMode; //是否支持码流纠错0-关闭；1-开启；
    private int voiceActivityDetection; //是否开启语音激励0-否；1-是；
    private int vacinterval; //语音激励敏感度(s)，最小值5s, 开启语音激励时有效
    private int callTimes;  //呼叫次数
    private int callInterval; //呼叫间隔(秒);
    private int callMode;  //呼叫模式0-手动呼叫；2-定时呼叫；
    private int cascadeMode; //是否级联上传0-否；1-是
    private int cascadeReturn; //是否级联回传0-否；1-是；
    private int cascadeReturnPara; //级联回传带宽参数
    private int vmpEnable;  //是否在合成0-否；1-是；
    private int mixEnable;  //是否在混音0-否；1-是；
    private int pollEnable; //是否在轮询0-否；1-是；
    private int needPassword; //是否需要密码0-否；1-是；
    private int oneReforming; //归一重整，开启该功能后可增强对外厂商终端的兼容，但会使丢包重传失效0-不启用；1-启用；
    private int doubleflow;  //成为发言人后立即发起内容共享0-否；1-是；
    private String platformId; //创会平台moid
    private List<VideoFormat> videoFormats;
    private Creator creator;

}
