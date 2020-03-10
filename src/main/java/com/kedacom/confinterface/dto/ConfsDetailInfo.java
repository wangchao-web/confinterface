package com.kedacom.confinterface.dto;

import java.util.List;

public class ConfsDetailInfo {
    public ConfsDetailInfo(String name, String confId, String groupId, int confLevel, String startTime, String endTime, String mcuIp) {
        this.name = name;
        this.confId = confId;
        this.groupId = groupId;
        this.confLevel = confLevel;
        this.startTime = startTime;
        this.endTime = endTime;
        this.mcuIp = mcuIp;
    }

    public ConfsDetailInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfId() {
        return confId;
    }

    public void setConfId(String confId) {
        this.confId = confId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getConfLevel() {
        return confLevel;
    }

    public void setConfLevel(int confLevel) {
        this.confLevel = confLevel;
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

    public String getMcuIp() {
        return mcuIp;
    }

    public void setMcuIp(String mcuIp) {
        this.mcuIp = mcuIp;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("name:").append(name)
                .append(", confId:").append(confId)
                .append(", groupId:").append(groupId)
                .append(", confLevel:").append(confLevel)
                .append(", startTime:").append(startTime)
                .append(", endTime:").append(endTime)
                .append(", mcuIp:").append(mcuIp)
                .toString();
    }

    private String name;   //会议名称
    private String confId; //会议Id
    private String groupId; //会议的调度组编号，因为是mcu开的会议，因此groupId由会议接入服务生成
    private int confLevel;  //会议级别，[1,16],值越小，级别越高
    private String startTime; //会议开始时间
    private String endTime ;  //会议结束时间
    private String mcuIp;     //会议所在的mcu的Ip地址

}
