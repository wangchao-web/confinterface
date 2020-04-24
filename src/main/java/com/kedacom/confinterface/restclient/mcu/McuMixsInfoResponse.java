package com.kedacom.confinterface.restclient.mcu;

import java.util.ArrayList;
import java.util.List;

public class McuMixsInfoResponse extends  McuBaseResponse {

    public McuMixsInfoResponse(int mode, List<TerminalId> members) {
        this.mode = mode;
        this.members = members;
    }

    public McuMixsInfoResponse() {
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
                .append("members:").append(members)
                .toString();
    }

    private int mode;
    List<TerminalId> members; //混音成员数组
}
