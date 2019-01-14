package com.kedacom.confinterface.restclient.mcu;

public class AccountTokenResponse extends McuBaseResponse {

    public AccountTokenResponse() {
        super();
        this.account_token = null;
    }

    public String getAccount_token() {
        return account_token;
    }

    public void setAccount_token(String account_token) {
        this.account_token = account_token;
    }

    private String account_token;
}
