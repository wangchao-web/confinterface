package com.kedacom.confinterface.dto;

import java.util.List;

public class QueryConfsCascadesMtsResponse extends BaseResponseMsg{

    public QueryConfsCascadesMtsResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public List<ConfsCascadesMtsInfo> getMts() {
        return mts;
    }

    public void setMts(List<ConfsCascadesMtsInfo> mts) {
        this.mts = mts;
    }

    private List<ConfsCascadesMtsInfo> mts;
}
