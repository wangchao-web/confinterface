package com.kedacom.confinterface.sip;

import com.kedacom.confinterface.service.TerminalService;


public class SipTerminalService extends TerminalService {

    public SipTerminalService(String e164, String name, boolean bVmt, SipProtocalConfig sipProtocalConfig){
        super(e164, name, bVmt);
        this.sipSrvIp = sipProtocalConfig.getSipServerIp();
        this.sipSrvPort = sipProtocalConfig.getSipServerPort();
        this.localIp = sipProtocalConfig.getBaseSysConfig().getLocalIp();
        this.localPort = sipProtocalConfig.getSipLocalPort();
    }

    private String localIp;
    private int localPort;
    private String sipSrvIp;
    private int sipSrvPort;
}
