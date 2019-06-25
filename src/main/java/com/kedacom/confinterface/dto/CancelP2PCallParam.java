package com.kedacom.confinterface.dto;

import javax.validation.constraints.NotBlank;

public class CancelP2PCallParam {
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public boolean isDual() {
        return dual;
    }

    public void setDual(boolean dual) {
        this.dual = dual;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("account:").append(account)
                .append(", dual:").append(dual)
                .toString();
    }

    @NotBlank
    private String account;

    private boolean dual;
}
