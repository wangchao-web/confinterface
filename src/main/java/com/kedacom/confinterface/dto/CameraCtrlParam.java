package com.kedacom.confinterface.dto;

import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;

public class CameraCtrlParam {
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("state:").append(state).append(", type:").append(type).toString();
    }

    @NotBlank
    private String resourceId;

    @Range(min = 0, max = 1)
    private int state;   //摄像头状态,0-开始,1-停止
    @Range(min = 1, max = 14)
    private int type;    //控制类型，1-上；2-下；3-左；4-右；5-上左；6-上右；7-下左；8-下右；9-视野小；10-视野大；11-调焦短；12-调焦长；13-亮度加；14-亮度减；
}
