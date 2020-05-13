package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;

import java.util.List;

public class SendSmsResponse extends BaseResponseMsg {

    public SendSmsResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public List<Terminal> getMtE164() {
        return mts;
    }

    public void setMtE164(List<Terminal> mts) {
        this.mts = mts;
    }

    private List<Terminal> mts;
}