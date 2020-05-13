package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class SendSmsParam {
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Terminal> getMts() {
        return mts;
    }

    public void setMts(List<Terminal> mts) {
        this.mts = mts;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRollNum() {
        return rollNum;
    }

    public void setRollNum(int rollNum) {
        this.rollNum = rollNum;
    }

    public int getRollSpeed() {
        return rollSpeed;
    }

    public void setRollSpeed(int rollSpeed) {
        this.rollSpeed = rollSpeed;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("type:").append(type)
                .append(", rollNum:").append(rollNum)
                .append(", rollSpeed:").append(rollSpeed)
                .append(", message:").append(message)
                .append(", mts:").append(mts)
                .toString();
    }

    @Valid
    @NotEmpty
    List<Terminal> mts;

    @Range(min = 0, max = 2)
    private int type = 0; // 短消息类型 0-自右至左滚动；1-翻页滚动；2-全页滚动；

    @Range(min = 1, max = 255)
    private int rollNum ; // 滚动次数 1-255 新版本终端255为无限轮询

    @Range(min = 1, max = 3)
    private int rollSpeed  = 1; //滚动速度 1-慢速；2-中速；3-快速；

    private String message; //消息内容,发送空消息即停止短消息 最大字符长度：1500个字节
}
