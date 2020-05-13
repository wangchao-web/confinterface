package com.kedacom.confinterface.dto;

public class MonitorsResponse extends BaseResponseMsg {
    public MonitorsResponse(int code, int status, String message, int type, String id, int mode) {
        super(code, status, message);
        this.type = type;
        this.id = id;
        this.mode = mode;
    }

    public MonitorsResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
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

    private int type;   //监控类型 1-终端；2-画面合成；3-混音；
    private String id;     //资源号，由流媒体返回
    private int mode;    //资源类型，"video","audio"
}
