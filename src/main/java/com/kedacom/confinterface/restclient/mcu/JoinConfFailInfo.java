package com.kedacom.confinterface.restclient.mcu;

public class JoinConfFailInfo {

    public JoinConfFailMt getJoinConfFailMt() {
        return joinConfFailMt;
    }

    public void setJoinConfFailMt(JoinConfFailMt joinConfFailMt) {
        this.joinConfFailMt = joinConfFailMt;
    }

    public String getOccupy_confname() {
        return occupy_confname;
    }

    public void setOccupy_confname(String occupy_confname) {
        this.occupy_confname = occupy_confname;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(joinConfFailMt).append(",occupy_confname:").append(occupy_confname).toString();
    }

    private JoinConfFailMt joinConfFailMt;
    private String occupy_confname;
}
