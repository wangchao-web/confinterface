package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class JoinDisscussionGroupParam {
    public List<Terminal> getMts() {
        return mts;
    }

    public void setMts(List<Terminal> mts) {
        this.mts = mts;
    }

    @Override
    public String toString() {
        if (null == mts)
            return "";

        return mts.toString();
    }

    @Valid
    @NotEmpty
    List<Terminal> mts;
}
