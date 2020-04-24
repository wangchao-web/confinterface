package com.kedacom.confinterface.restclient.mcu;

import java.util.List;

public class McuMixMembers {
    public McuMixMembers(List<TerminalId> members) {
        this.members = members;
    }

    public McuMixMembers() {
    }

    public List<TerminalId> getMembers() {
        return members;
    }

    public void setMembers(List<TerminalId> members) {
        this.members = members;
    }

    List<TerminalId> members; //增加或者删除混音成员数组
}
