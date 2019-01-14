package com.kedacom.confinterface.dto;

import javax.validation.constraints.NotBlank;

public class SubscribeTerminalStatusParam {
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "url:"+url;
    }

    @NotBlank
    private String url;
}
