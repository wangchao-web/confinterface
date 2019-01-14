package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class JoinConferenceParam {
    public void setMts(List<Terminal> mts) {
        this.mts = mts;
    }

    public List<Terminal> getMts() {
        return mts;
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
