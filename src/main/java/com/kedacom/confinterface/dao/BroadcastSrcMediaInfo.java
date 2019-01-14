package com.kedacom.confinterface.dao;

import java.io.Serializable;

public class BroadcastSrcMediaInfo implements Serializable {
    public BroadcastSrcMediaInfo(){
        super();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setMtE164(String mtE164) {
        this.mtE164 = mtE164;
    }

    public String getMtE164() {
        return mtE164;
    }

    public String getVmtE164() {
        return vmtE164;
    }

    public void setVmtE164(String vmtE164) {
        this.vmtE164 = vmtE164;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("mtE164:").append(mtE164).append(",Type:").append(type).toString();
    }

    private int type;  //广播源类型：0-未知，1-终端，2-混音/画面合成/监控前端
    private String mtE164;
    private String vmtE164;  //保存作为广播使用的vmt的E164号
}
