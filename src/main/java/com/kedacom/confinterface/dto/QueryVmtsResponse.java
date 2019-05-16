package com.kedacom.confinterface.dto;

import java.util.List;

public class QueryVmtsResponse extends BaseResponseMsg {

    public QueryVmtsResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public List<VmtDetailInfo> getVmts() {
        return vmts;
    }

    public void setVmts(List<VmtDetailInfo> vmts) {
        this.vmts = vmts;
    }

    private List<VmtDetailInfo> vmts;
}
