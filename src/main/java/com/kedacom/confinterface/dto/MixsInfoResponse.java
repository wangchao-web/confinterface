package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;
import java.util.List;

public class MixsInfoResponse extends  BaseResponseMsg {

    public MixsInfoResponse(int code, int status, String message, int mode, List<Terminal> members) {
        super(code, status, message);
        this.mode = mode;
        this.members = members;
    }

    public MixsInfoResponse(int code, int status, String message) {
        super(code, status, message);
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public List<Terminal> getMembers() {
        return members;
    }

    public void setMembers(List<Terminal> members) {
        this.members = members;
    }

    private int mode;//混音模式1-智能混音；2-定制混音；
    private List<Terminal> members; //混音成员数组
}
