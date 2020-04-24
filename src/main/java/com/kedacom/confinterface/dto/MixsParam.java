package com.kedacom.confinterface.dto;

import com.kedacom.confinterface.dao.Terminal;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;


public class MixsParam {
    public MixsParam(@Range(min = 1, max = 2) int mode, List<Terminal> members) {
        this.mode = mode;
        this.members = members;
    }

    public MixsParam() {
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

    @Range(min = 1, max = 2)
    private int mode;//混音模式1-智能混音；2-定制混音；

    @Valid
    @NotEmpty
    private List<Terminal> members; //混音成员数组
}
