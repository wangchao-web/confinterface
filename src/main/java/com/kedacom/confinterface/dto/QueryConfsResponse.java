package com.kedacom.confinterface.dto;

import java.util.List;

public class QueryConfsResponse extends  BaseResponseMsg{
    public QueryConfsResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public List<ConfsDetailInfo> getConfs() {
        return confs;
    }

    public void setConfs(List<ConfsDetailInfo> confs) {
        this.confs = confs;
    }

    private List<ConfsDetailInfo> confs;
}


