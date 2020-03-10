package com.kedacom.confinterface.dto;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class TerminalMtId implements Serializable {

    public TerminalMtId(){
        super();
        this.mtId = null;
    }

    public TerminalMtId(String mtId) {
        super();
        this.mtId = mtId;
    }

    public String getMtId() {
        return mtId;
    }

    public void setMtId(String mtId) {
        this.mtId = mtId;
    }

    @Override
    public String toString() {
        return mtId;
    }

    @NotBlank
    protected String mtId;
}
