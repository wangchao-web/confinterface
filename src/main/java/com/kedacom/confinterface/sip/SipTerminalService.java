package com.kedacom.confinterface.sip;

import com.kedacom.confadapter.MediaDescription;
import com.kedacom.confinterface.dto.BaseRequestMsg;
import com.kedacom.confinterface.service.TerminalService;

import java.util.Vector;


public class SipTerminalService extends TerminalService {

    public SipTerminalService(String e164, String name, boolean bVmt, SipProtocalConfig sipProtocalConfig){
        super(e164, name, bVmt);
        this.sipSrvIp = sipProtocalConfig.getSipServerIp();
        this.sipSrvPort = sipProtocalConfig.getSipServerPort();
        this.localIp = sipProtocalConfig.getBaseSysConfig().getLocalIp();
        this.localPort = sipProtocalConfig.getSipLocalPort();
    }

    @Override
    public boolean closeDualStreamChannel() {
        return false;
    }

    @Override
    public boolean onOpenLogicalChannel(Vector<MediaDescription> mediaDescriptions) {
        return false;
    }

    @Override
    public void openDualStreamChannel(BaseRequestMsg startDualStreamRequest) {

    }

    private String localIp;
    private int localPort;
    private String sipSrvIp;
    private int sipSrvPort;
}
