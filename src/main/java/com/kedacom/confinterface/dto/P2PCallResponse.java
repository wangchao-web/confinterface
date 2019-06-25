package com.kedacom.confinterface.dto;

import java.util.List;

public class P2PCallResponse extends BaseResponseMsg {

    public P2PCallResponse(int code, int status, String message){
        super(code, status, message);
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public List<MediaResource> getForwardResources() {
        return forwardResources;
    }

    public void setForwardResources(List<MediaResource> forwardResources) {
        this.forwardResources = forwardResources;
    }

    public List<MediaResource> getReverseResources() {
        return reverseResources;
    }

    public void setReverseResources(List<MediaResource> reverseResources) {
        this.reverseResources = reverseResources;
    }

    private String account;
    private List<MediaResource> forwardResources;
    private List<MediaResource> reverseResources;
}
