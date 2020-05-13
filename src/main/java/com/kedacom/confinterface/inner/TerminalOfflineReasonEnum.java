package com.kedacom.confinterface.inner;

public enum TerminalOfflineReasonEnum {

    OK(200, "呼叫OK"),
    None(0,"None 终端未给原因"),
    Busy(1,"Busy 正忙"),
    Normal(2,"Normal 对端正常挂断"),
    Rejected(3,"Rejected 对端拒绝"),
    Unreachable(4,"Unreachable 对端不可达"),
    Local(5,"Local 本地挂断"),
    Unknown(6,"Unknown 未知"),
    NoBandwidth(7,"NoBandwidth 带宽不足"),
    NoPermission(8, "NoPermission 没有权限"),
    UnreachableGatekeeper(9,"UnreachableGatekeeper GK不可达"),
    McuOccupy(10,"McuOccupy Mcu占用"),
    Reconnect(11,"Reconnect 重连接"),
    ConfHolding(12,"ConfHolding 会议正举行"),
    Hascascaded(13,"Hascascaded 已级联"),
    Custom(14,"Custom 自定义"),
    Adaptivebusy(15,"Adaptivebusy 适配器忙"),
    NmediaResource(16,"流媒体创建资源或者更新失败");
    public int getCode(){
        return this.code;
    }

    public String getReason(){
        return this.reason;
    }
    TerminalOfflineReasonEnum(int code, String reason){
        this.code = code;
        this.reason = reason;
    }

    private int code;
    private String reason;
}
