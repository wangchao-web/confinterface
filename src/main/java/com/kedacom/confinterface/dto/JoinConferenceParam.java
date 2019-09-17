package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;
import org.hibernate.validator.constraints.Range;

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

    public int getConfinterface() {
        return confinterface;
    }

    public void setConfinterface(int confinterface) {
        this.confinterface = confinterface;
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

    @Range(min = 0, max = 1)
    private int confinterface ; //0是vmt终端入会,1是纯终端入会
}
