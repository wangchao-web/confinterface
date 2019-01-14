package com.kedacom.confinterface.restclient.mcu;

import net.sf.json.JSONObject;
import java.net.URLEncoder;

public class McuPostMsg {

    public McuPostMsg(String accountToken) {
        super();
        this.account_token = accountToken;
        this.params = null;
    }

    public String getAccount_token() {
        return account_token;
    }

    public void setAccount_token(String account_token) {
        this.account_token = account_token;
    }

    public String getParams() {
        return params;
    }

    public boolean setParams(Object params) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(params);
            this.params = URLEncoder.encode(jsonObject.toString(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public String getMsg(){
        StringBuffer msg = new StringBuffer();
        if (null != params) {
            msg.append("params=");
            msg.append(this.params);
            msg.append("&");
        }

        msg.append("account_token=");
        msg.append(this.account_token);
        return msg.toString();
    }

    private String account_token;
    private String params;
}
