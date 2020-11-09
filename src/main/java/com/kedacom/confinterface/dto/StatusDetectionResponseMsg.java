package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Connstatus;

import java.util.ArrayList;
import java.util.List;

public class StatusDetectionResponseMsg {

    public StatusDetectionResponseMsg(List<Connstatus> connstatuses) {
        this.connstatuses = connstatuses;
    }

    public StatusDetectionResponseMsg() {
        this.connstatuses = new ArrayList<>();
    }

    public List<Connstatus> getConnstatuses() {
        return connstatuses;
    }

    public void setConnstatuses(List<Connstatus> connstatuses) {
        this.connstatuses = connstatuses;
    }

    private List<Connstatus> connstatuses;
}
