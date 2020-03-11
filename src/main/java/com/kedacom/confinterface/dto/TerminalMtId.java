package com.kedacom.confinterface.dto;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class TerminalMtId implements Serializable {

    public TerminalMtId(){
        super();
        this.account = null;
    }

    public TerminalMtId(@NotBlank String account) {
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return account;
    }

    @NotBlank
    protected String account;
}
