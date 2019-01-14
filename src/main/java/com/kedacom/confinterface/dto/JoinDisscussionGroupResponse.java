package com.kedacom.confinterface.dto;

import java.util.List;

public class JoinDisscussionGroupResponse extends BaseResponseMsg {
    public JoinDisscussionGroupResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public List<TerminalMediaResource> getMtMediaResources() {
        return mtMediaResources;
    }

    public void setMtMediaResources(List<TerminalMediaResource> mtMediaResources) {
        this.mtMediaResources = mtMediaResources;
    }

    private List<TerminalMediaResource> mtMediaResources;
}
