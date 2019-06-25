package com.kedacom.confinterface.dto;

import org.hibernate.validator.constraints.Range;
import javax.validation.constraints.NotBlank;

public class P2PCallParam {
    public P2PCallParam(int accountType, String account, boolean dual){
        this.accountType = accountType;
        this.account = account;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

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
        return new StringBuilder().append("accountType:").append(accountType)
                .append(", account:").append(account)
                .append(", dual:").append(dual)
                .toString();
    }

    @Range(min = 1, max = 2)
    private int accountType;

    @NotBlank
    private String account;

    private boolean dual;
}
