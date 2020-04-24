package com.kedacom.confinterface.dto;

public class MonitorsResponse extends BaseResponseMsg {
    public MonitorsResponse(int code, int status, String message, String type, String id, int mode) {
        super(code, status, message);
        this.type = type;
        this.id = id;
        this.mode = mode;
    }

    public MonitorsResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("type:").append(type)
                .append(", id:").append(id)
                .append(", mode:").append(mode)
                .toString();
    }

    private String type;   //资源类型，"video","audio"
    private String id;      //资源号，由流媒体返回
    private int mode; //监控类型 1-终端；2-画面合成；3-混音；
}
