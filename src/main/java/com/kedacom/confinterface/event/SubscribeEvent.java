package com.kedacom.confinterface.event;

import org.springframework.context.ApplicationEvent;

public class SubscribeEvent extends ApplicationEvent {

    public SubscribeEvent(Object source, String confId, String method, int errorCode, String channel, Object content) {
        super(source);
        this.confId = confId;
        this.method = method;
        this.errorCode = errorCode;
        this.channel = channel;
        this.content = content;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public void setConfId(String confId) {
        this.confId = confId;
    }

    public String getConfId() {
        return confId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    private String confId;
    private String method;
    private int errorCode;
    private String channel;
    private Object content;
}
