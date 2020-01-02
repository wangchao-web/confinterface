package com.kedacom.confinterface.inner;

import com.kedacom.confinterface.service.TerminalService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class P2PCallGroup {
    public P2PCallGroup(String groupId){
        super();
        this.groupId = groupId;
        this.callMemberMap = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, TerminalService> getCallMap() {
        return callMemberMap;
    }

    public void addCallMember(String mtAccount, TerminalService vmtService) {
        callMemberMap.put(mtAccount, vmtService);
    }

    public void removeCallMember(String mtAccount){
        callMemberMap.remove(mtAccount);
    }

    public TerminalService getVmt(String mtAccount){
        return callMemberMap.get(mtAccount);
    }

    public TerminalService getVmtByResourceId(String resourceId){
        for (Map.Entry<String, TerminalService> callMember : callMemberMap.entrySet()) {
            TerminalService terminalService  = callMember.getValue();

            if (terminalService.hasResourceId(true, resourceId))
                return terminalService;
        }

        return null;
    }

    private String groupId;
    private ConcurrentHashMap<String, TerminalService> callMemberMap;        //key为呼叫的终端帐号,value为对应的虚拟终端
}
