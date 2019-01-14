package com.kedacom.confinterface.dto;

import java.util.List;

public class LeftDiscussionGroupResponse extends BaseResponseMsg{
    public LeftDiscussionGroupResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public void setMts(List<LeftDiscussionGroupMt> mts) {
        this.mts = mts;
    }

    public List<LeftDiscussionGroupMt> getMts() {
        return mts;
    }

    private List<LeftDiscussionGroupMt> mts;
}
