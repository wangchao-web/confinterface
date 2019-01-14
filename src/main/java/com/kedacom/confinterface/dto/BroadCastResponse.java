package com.kedacom.confinterface.dto;

import java.util.List;

public class BroadCastResponse extends BaseResponseMsg {

    public BroadCastResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getMtE164() {
        return mtE164;
    }

    public void setMtE164(String mtE164) {
        this.mtE164 = mtE164;
    }

    public void setForwardResources(List<MediaResource> forwardResources) {
        this.forwardResources = forwardResources;
    }

    public List<MediaResource> getForwardResources() {
        return forwardResources;
    }

    public void setReverseResources(List<MediaResource> reverseResources) {
        this.reverseResources = reverseResources;
    }

    public List<MediaResource> getReverseResources() {
        return reverseResources;
    }

    private int type;  //广播源类型：1-终端，2-混音/画面合成/监控前端
    private String mtE164;
    private List<MediaResource> forwardResources;
    private List<MediaResource> reverseResources;
}
