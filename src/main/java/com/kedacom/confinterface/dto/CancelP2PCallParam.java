package com.kedacom.confinterface.dto;

import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;

public class CancelP2PCallParam {
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public boolean isDual() {
        if(dual == 1){
            return true;
        }else if(dual == 0){
            return false;
        }
        return false;
    }

    public int getDual() {
        return dual;
    }

    public void setDual(int dual) {
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

    @Range(min = 0, max = 1)
    private int dual;
}
