package com.kedacom.confinterface.dto;

public class SendIFrameResponse extends BaseResponseMsg {

    public SendIFrameResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public SendIFrameParam getSendIFrameParam() {
        return sendIFrameParam;
    }

    public void setSendIFrameParam(SendIFrameParam sendIFrameParam) {
        this.sendIFrameParam = sendIFrameParam;
    }

    private SendIFrameParam sendIFrameParam;
}
