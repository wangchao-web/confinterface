package com.kedacom.confinterface.dto;

import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

public class BroadCastParam {
    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setMtE164(String mtE164) {
        this.mtE164 = mtE164;
    }

    public String getMtE164() {
        return mtE164;
    }

    public boolean isTerminalType() {
        if (type == 1)
            return true;
        return false;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("type:").append(type).append(", mtE164:").append(mtE164).toString();
    }

    @Range(min = 1, max = 2)
    protected int type; //广播源类型：1-终端，2-混音/画面合成/监控前端
    @NotNull
    protected String mtE164;
}
