package com.kedacom.confinterface.exchange;

public class CreateResourceParam {
    public CreateResourceParam(){
        super();
    }

    public void setSdp(String sdp) {
        this.sdp = sdp;
    }

    public String getSdp() {
        return sdp;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(", sdp:").append(sdp).toString();
    }

    private String sdp;
}
