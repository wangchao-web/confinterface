package com.kedacom.confinterface.dto;


import org.hibernate.validator.constraints.Range;
import javax.validation.constraints.NotBlank;


public class P2PCallParam {
    public P2PCallParam(int accountType, String account, int dual){
        this.accountType = accountType;
        this.account = account;
        this.dual = dual;
    }

    public P2PVideoCallMediaCap getVideoCodec() {
        return videoCodec;
    }

    public void setVideoCodec(P2PVideoCallMediaCap videoCodec) {
        this.videoCodec = videoCodec;
    }

    public P2PAudioCallMediaCap getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(P2PAudioCallMediaCap audioCodec) {
        this.audioCodec = audioCodec;
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
        return new StringBuilder().append("accountType:").append(accountType)
                .append(", account:").append(account)
                .append(", dual:").append(dual)
                .append(", videoCodec:").append(videoCodec)
                .append(", audioCodec:").append(audioCodec)
                .toString();
    }

    @Range(min = 1, max = 3)
    private int accountType;

    @NotBlank
    private String account;

    @Range(min = 0, max = 1)
    private int dual;

    private P2PVideoCallMediaCap videoCodec;
    private P2PAudioCallMediaCap audioCodec;
}
