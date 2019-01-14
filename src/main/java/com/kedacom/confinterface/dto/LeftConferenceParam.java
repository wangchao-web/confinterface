package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class LeftConferenceParam {

    public LeftConferenceParam() {
        super();
    }

    public boolean isCancelGroup() {
        return cancelGroup;
    }

    public void setCancelGroup(boolean cancelGroup) {
        this.cancelGroup = cancelGroup;
    }

    public List<Terminal> getMts() {
        return mts;
    }

    public void setMts(List<Terminal> mts) {
        this.mts = mts;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("cancelGroup:").append(cancelGroup)
                .append(", mts:").append(mts)
                .toString();
    }

    protected boolean cancelGroup;

    @Valid
    @NotEmpty
    protected List<Terminal> mts;
}
