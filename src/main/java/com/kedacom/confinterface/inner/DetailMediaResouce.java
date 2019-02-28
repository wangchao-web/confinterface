package com.kedacom.confinterface.inner;

import com.kedacom.confinterface.dto.MediaResource;

import java.util.concurrent.atomic.AtomicInteger;

public class DetailMediaResouce extends MediaResource {

    public DetailMediaResouce(MediaResource mediaResource){
        super();
        this.streamIndex = new AtomicInteger(-1);
        this.setId(mediaResource.getId());
        this.setType(mediaResource.getType());
        this.setDual((mediaResource.getDual()==1));
    }

    public DetailMediaResouce(){
        super();
        this.streamIndex = new AtomicInteger(-1);
    }

    public int getStreamIndex() {
        return streamIndex.get();
    }

    public boolean compareAndSetStreamIndex(int expect, int update){
        return streamIndex.compareAndSet(expect, update);
    }

    public void setStreamIndex(int streamIndex) {
        this.streamIndex.set(streamIndex);
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

    private AtomicInteger streamIndex;
    private String sdp;
    private TransportAddress rtp;
    private TransportAddress rtcp;
}
