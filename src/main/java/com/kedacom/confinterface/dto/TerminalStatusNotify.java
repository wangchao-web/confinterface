package com.kedacom.confinterface.dto;


import java.util.ArrayList;
import java.util.List;

public class TerminalStatusNotify extends BasePublishMsg{

    public List<TerminalStatus> getMtStatusNotify() {
        return mtStatusNotify;
    }

    public void setMtStatusNotify(List<TerminalStatus> mtStatusNotify) {
        this.mtStatusNotify = mtStatusNotify;
    }

    public void addMtStatus(TerminalStatus terminalStatus){
        synchronized (this){
            if (null == mtStatusNotify){
                mtStatusNotify = new ArrayList<>();
            }
        }

        mtStatusNotify.add(terminalStatus);
    }

    private List<TerminalStatus> mtStatusNotify;

}
