package com.kedacom.confinterface.restclient.mcu;

public class Creator {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getAccount_type() {
        return account_type;
    }

    public void setAccount_type(int account_type) {
        this.account_type = account_type;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("name:").append(name)
                .append(", account:").append(account)
                .append(", account_type:").append(account_type)
                .append(", telephone:").append(telephone)
                .append(", mobile:").append(mobile)
                .toString();
    }

    private String name;
    private String account;
    private int account_type;
    private String telephone;
    private String mobile;
}
