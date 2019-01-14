package com.kedacom.confinterface.dto;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class MediaResource implements Serializable {

    public MediaResource(){
        super();
        this.type = null;
        this.id = null;
        this.dual = new AtomicInteger();
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDual(int dual) {
        this.dual.set(dual);
    }

    public int getDual() {
        return dual.get();
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("type:").append(type)
                .append(", id:").append(id)
                .append(", dual:").append(dual.get())
                .toString();
    }

    private String type;   //资源类型，"video","audio"
    private String id;      //资源号，由流媒体返回
    private AtomicInteger dual;   //是否双流 0-否，1-是
}
