package com.kedacom.confinterface.inner;

import com.kedacom.confinterface.dto.MediaResource;

public class DetailMediaResouce extends MediaResource {

    public DetailMediaResouce(MediaResource mediaResource){
        super();
        this.streamIndex = -1;
        this.setId(mediaResource.getId());
        this.setType(mediaResource.getType());
        this.setDual((mediaResource.getDual()==1));
    }

    public DetailMediaResouce(){
        super();
    }

    public int getStreamIndex() {
        return streamIndex;
    }

    public void setStreamIndex(int streamIndex) {
        this.streamIndex = streamIndex;
    }

    public TransportAddress getRtcp() {
        return rtcp;
    }

    public void setRtcp(TransportAddress rtcp) {
        this.rtcp = rtcp;
    }

    public void setRtp(TransportAddress rtp) {
        this.rtp = rtp;
    }

    public TransportAddress getRtp() {
        return rtp;
    }

    public String getSdp() {
        return sdp;
    }

    public void setSdp(String sdp) {
        this.sdp = sdp;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString())
                .append(", streamIndex:").append(streamIndex)
                .append(", sdp:").append(sdp)
                .append(", rtp:").append(rtp)
                .append(", rtcp:").append(rtcp)
                .toString();
    }

    private int streamIndex;
    private String sdp;
    private TransportAddress rtp;
    private TransportAddress rtcp;
}
