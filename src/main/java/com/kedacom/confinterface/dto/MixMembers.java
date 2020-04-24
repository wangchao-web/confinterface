package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class MixMembers {
    public List<Terminal> getMembers() {
        return members;
    }

    public void setMembers(List<Terminal> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        if (null == members)
            return "";

        return members.toString();
    }

    @Valid
    @NotEmpty
    List<Terminal> members;
}
