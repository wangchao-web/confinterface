package com.kedacom.confinterface.restclient.mcu;



import java.util.List;

public class ConfsInfoResponse extends McuBaseResponse {

    public ConfsInfoResponse() {
        super();
        this.confs = null;
    }

    public List<ConfsDetailRspInfo> getConfs() {
        return confs;
    }

    public void setConfs(List<ConfsDetailRspInfo> confs) {
        this.confs = confs;
    }

    private List<ConfsDetailRspInfo> confs;
}
