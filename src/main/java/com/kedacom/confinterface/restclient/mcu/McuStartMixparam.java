package com.kedacom.confinterface.restclient.mcu;

import java.util.List;

public class McuStartMixparam {

    public McuStartMixparam(int mode, List<TerminalId> members) {
        this.mode = mode;
        this.members = members;
    }

    public McuStartMixparam() {
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public List<TerminalId> getMembers() {
        return members;
    }

    public void setMembers(List<TerminalId> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("mode:").append(mode)
                .append(", members:").append(members.toString())
                .toString();
    }

    private int mode;
    List<TerminalId> members; //混音成员数组
}
