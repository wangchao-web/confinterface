package com.kedacom.confinterface.restclient.mcu;

public class LoginResponse extends McuBaseResponse {

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String username;
}
