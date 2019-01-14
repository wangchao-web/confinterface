package com.kedacom.confinterface.restclient.mcu;

public class JoinConfFailMt {

    public int getAccount_type() {
        return account_type;
    }

    public void setAccount_type(int account_type) {
        this.account_type = account_type;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("account:").append(account).append(",account_type:").append(account_type).toString();
    }

    private String account;
    private int account_type;   //终端类型，5-e164号码; 6-电话; 7-ip地址
}
