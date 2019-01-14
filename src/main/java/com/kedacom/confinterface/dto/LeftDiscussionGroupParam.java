package com.kedacom.confinterface.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class LeftDiscussionGroupParam {

    public void setMts(List<LeftDiscussionGroupMt> mts) {
        this.mts = mts;
    }

    public List<LeftDiscussionGroupMt> getMts() {
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
    private List<LeftDiscussionGroupMt> mts;
}
