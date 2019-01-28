package com.kedacom.confinterface.dto;

import java.util.List;

public class StartDualResponse extends BaseResponseMsg {

    public StartDualResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public List<MediaResource> getResources() {
        return resources;
    }

    public void setResources(List<MediaResource> resources) {
        this.resources = resources;
    }

    private List<MediaResource> resources;
}
