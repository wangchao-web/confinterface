package com.kedacom.confinterface.dto;

import org.springframework.http.HttpStatus;

public class BaseResponseMsg {

    public BaseResponseMsg(){
        super();
    }

    public BaseResponseMsg(int code, int status, String message) {
        super();
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public void error(int code, String errMsg) {
        this.status = HttpStatus.OK.value();
        this.code = code;
        this.message = errMsg;
        this.timestamp = System.currentTimeMillis();
    }

    public void success() {
        this.status = HttpStatus.OK.value();
        this.code = 0;
        this.message = "OK";
        this.timestamp = System.currentTimeMillis();
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private int code;
    private String message;
    private int status;
    private long timestamp;
}
