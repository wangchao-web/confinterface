package com.kedacom.confinterface.dto;

import java.util.List;

public class QueryDualStreamResponse extends BaseResponseMsg {
    public QueryDualStreamResponse(int code, int status, String message){
        super(code, status, message);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<MediaResource> getResources() {
        return resources;
    }

    public void setResources(List<MediaResource> resources) {
        this.resources = resources;
    }

    private int type;     //1-终端，2-监控前端
    private List<MediaResource> resources;
}
