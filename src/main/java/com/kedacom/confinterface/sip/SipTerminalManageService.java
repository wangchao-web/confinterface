package com.kedacom.confinterface.sip;

import com.kedacom.confinterface.service.TerminalManageService;
import com.kedacom.confinterface.service.TerminalService;

public class SipTerminalManageService extends TerminalManageService {

    public SipTerminalManageService(SipProtocalConfig sipProtocalConfig) {
        super();
        this.sipProtocalConfig = sipProtocalConfig;
    }

    public SipProtocalConfig getSipProtocalConfig() {
        return sipProtocalConfig;
    }

    public void StartUp() {
    }

    public TerminalService createTerminal(String e164, boolean bVmt) {
        StringBuffer name = new StringBuffer();

        if (bVmt) {
            name.append(sipProtocalConfig.getBaseSysConfig().getVmtNamePrefix());
            name.append(e164);
        } else {
            name.append("sipmt_");
            name.append(e164);
        }

        SipTerminalService sipTerminalService = new SipTerminalService(e164, name.toString(), bVmt, sipProtocalConfig);
        createConfParticipant(sipTerminalService);

        if (bVmt)
            freeVmtServiceMap.put(e164, sipTerminalService);

        return sipTerminalService;
    }

    private SipProtocalConfig sipProtocalConfig;
}
