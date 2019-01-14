package com.kedacom.confinterface.restclient.mcu;

import com.kedacom.confinterface.util.ProtocalTypeEnum;

public class JoinConferenceMtInfo {

    public JoinConferenceMtInfo() {
        super();
        this.account_type = 5; //终端类型, 5-e164号码；6-电话；7-ip地址；8-别名@ip(监控前端)
        this.bitrate = 4096;
        this.protocol = ProtocalTypeEnum.H323.getCode();
        this.forced_call = 0;
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

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getForced_call() {
        return forced_call;
    }

    public void setForced_call(int forced_call) {
        this.forced_call = forced_call;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("account:").append(account)
                .append(",account_type:").append(account_type)
                .append(",bitrate:").append(bitrate)
                .append(",protocol:").append(protocol)
                .append(",forced_call:").append(forced_call)
                .toString();
    }

    private String account;
    private int account_type;   //终端类型, 5-e164号码；6-电话；7-ip地址；8-别名@ip(监控前端)
    private int bitrate;
    private int protocol;   	//呼叫协议, 0-H323, 1-SIP
    private int forced_call;
}
