package com.kedacom.confinterface.restclient.mcu;

public class JoinConferenceRspMtInfo {

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setAccount_type(int account_type) {
        this.account_type = account_type;
    }

    public int getAccount_type() {
        return account_type;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setMt_id(String mt_id) {
        this.mt_id = mt_id;
    }

    public String getMt_id() {
        return mt_id;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("account:").append(account)
                .append(",account_type:").append(account_type)
                .append(",protocol:").append(protocol)
                .append(",mt_id:").append(mt_id)
                .toString();
    }

    private String account;
    private int account_type;
    private int protocol;
    private String mt_id;
}
