package com.kedacom.confinterface.dto;

import java.util.List;

public class QueryConfCascadesInfoResponse extends  BaseResponseMsg {
    public QueryConfCascadesInfoResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public List<ConfsCascadesInfo> getCascades() {
        return cascades;
    }

    public void setCascades(List<ConfsCascadesInfo> cascades) {
        this.cascades = cascades;
    }

    private List<ConfsCascadesInfo> cascades;
}
