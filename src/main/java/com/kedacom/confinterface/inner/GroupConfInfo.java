package com.kedacom.confinterface.inner;

import com.kedacom.confinterface.dao.InspectionSrcParam;
import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.dto.BaseRequestMsg;
import com.kedacom.confinterface.dto.BaseResponseMsg;
import com.kedacom.confinterface.service.TerminalService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroupConfInfo {

    public GroupConfInfo(String groupId, String confId) {
        super();
        this.groupId = groupId;
        this.confId = confId;
        this.broadcastType = 0;
        this.broadcastVmtService = null;
        this.broadcastMtE164 = null;
        this.freeVmtMembers = new ConcurrentHashMap<>();
        this.mtMembers = new ConcurrentHashMap<>();
        this.usedVmtMembers = new ConcurrentHashMap<>();
        this.mtIdMap = new ConcurrentHashMap<>();
        this.waitDealTask = null;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getConfId() {
        return confId;
    }

    public void setConfId(String confId) {
        this.confId = confId;
    }

    public String getBroadcastMtE164() {
        return broadcastMtE164;
    }

    public TerminalService getBroadcastVmtService() {
        return broadcastVmtService;
    }

    public void setBroadcastMtE164(String broadcastMtE164) {
        this.broadcastMtE164 = broadcastMtE164;
    }

    public void setBroadcastVmtService(TerminalService terminalService) {
        if (null != terminalService){
            this.broadcastVmtService = terminalService;
            this.broadcastVmtService.setSupportDualStream(true);
            return;
        }

        synchronized (freeVmtMembers){
            for(Map.Entry<String, TerminalService> vmt : freeVmtMembers.entrySet()){
                this.broadcastVmtService = vmt.getValue();
                break;
            }

            if (null == broadcastVmtService){
                return;
            }

            this.broadcastVmtService.setSupportDualStream(true);
            freeVmtMembers.remove(broadcastVmtService.getE164());
        }
    }

    public boolean isTerminalType() {
        if (1 == broadcastType)
            return true;
        return false;
    }

    public int getBroadcastType() {
        return broadcastType;
    }

    public void setBroadcastType(int broadcastType) {
        this.broadcastType = broadcastType;
    }

    public Map<String, TerminalService> getMtMembers() {
        return mtMembers;
    }

    public TerminalService getMtMember(String mtE164) {
        return mtMembers.get(mtE164);
    }

    public TerminalService delMtMember(String mtE164) {
        synchronized (this) {
            TerminalService terminalService = mtMembers.get(mtE164);
            if (null == terminalService)
                return null;

            String mtId = terminalService.getMtId();
            if (null != mtId)
                mtIdMap.remove(terminalService.getMtId());

            return mtMembers.remove(mtE164);
        }
    }

    public Map<String, TerminalService> getUsedVmtMembers() {
        return usedVmtMembers;
    }

    public TerminalService getNoInspectTerminalServiceFromUsedVmtMember(){
        //return usedVmtMembers.search(1, (k,v)->v.getInspectionParam() == null ? v : null);
        synchronized (usedVmtMembers){
            for (Map.Entry<String, TerminalService> usedVmt : usedVmtMembers.entrySet()){
                if (usedVmt.getValue().getInspectionParam() == null){
                    return usedVmt.getValue();
                }
            }

            return null;
        }
    }

    public TerminalService getNotBeInspectedTerminalServiceFromUsedVmtMember(){
        synchronized (usedVmtMembers){
            for (Map.Entry<String, TerminalService> usedVmt : usedVmtMembers.entrySet()){
                TerminalService vmtService = usedVmt.getValue();
                if (null == vmtService.getInspentedTerminals()
                        || vmtService.getInspentedTerminals().isEmpty()){
                    return vmtService;
                }
            }

            return null;
        }
    }

    public TerminalService getVmtMember(String vmtE164) {
        TerminalService terminalService = freeVmtMembers.get(vmtE164);
        if (null != terminalService)
            return terminalService;

        return usedVmtMembers.get(vmtE164);
    }

    public void delVmtMembers(List<Terminal> vmts){
        synchronized (this) {
            for (Terminal vmt : vmts) {
                if (null != broadcastVmtService && broadcastVmtService.getE164().equals(vmt.getMtE164())) {
                    String mtId = broadcastVmtService.getMtId();
                    if (null != mtId)
                        mtIdMap.remove(mtId);

                    broadcastVmtService = null;
                    continue;
                }

                TerminalService vmtService = freeVmtMembers.remove(vmt.getMtE164());
                if (null == vmtService) {
                    vmtService = usedVmtMembers.remove(vmt.getMtE164());
                    if (null == vmtService)
                        continue;
                }

                vmtService.setGroupId(null);

                String mtId = vmtService.getMtId();
                if (null != mtId)
                    mtIdMap.remove(mtId);
            }
        }
    }

    public void addMember(TerminalService member) {
        if (member.isVmt()) {
            freeVmtMembers.put(member.getE164(), member);
        } else {
            mtMembers.put(member.getE164(), member);
        }
    }

    public void delMember(TerminalService member){
        if (member.isVmt()){
            TerminalService removeService = freeVmtMembers.remove(member.getE164());
            if (null == removeService)
                usedVmtMembers.remove(member.getE164());
        } else {
            mtMembers.remove(member.getE164());
        }

        String mtId = member.getMtId();
        if (null != mtId)
            mtIdMap.remove(mtId);
    }

    public TerminalService getMember(String e164){
        if (null != broadcastVmtService && broadcastVmtService.getE164().equals(e164))
            return broadcastVmtService;

        TerminalService terminalService = mtMembers.get(e164);
        if (null != terminalService)
            return terminalService;

        terminalService = freeVmtMembers.get(e164);
        if (null != terminalService)
            return terminalService;

        terminalService = usedVmtMembers.get(e164);
        if (null != terminalService)
            return terminalService;

        return null;
    }

    public void addWaitDealTask(String subcribeChannel, BaseRequestMsg<? extends BaseResponseMsg> requestMsg) {
        if (null == waitDealTask) {
            synchronized (this) {
                waitDealTask = new ConcurrentHashMap<>();
            }
        }

        waitDealTask.put(subcribeChannel, requestMsg);
    }

    public void addMtId(String mtId, String e164) {
        mtIdMap.put(mtId, e164);
    }

    public String getE164(String mtId) {
        if (null == mtIdMap)
            return null;

        return mtIdMap.get(mtId);
    }

    public BaseRequestMsg<? extends BaseResponseMsg> getWaitDealTask(String channel) {
        if (null == waitDealTask)
            return null;

        return waitDealTask.get(channel);
    }

    public void delWaitDealTask(String channel) {
        if (null == waitDealTask)
            return;

        waitDealTask.remove(channel);
    }

    public TerminalService getAndUseVmt(String vmtE164) {
        synchronized (freeVmtMembers) {
            TerminalService freeTerminalService = null;
            if (null != vmtE164) {
                freeTerminalService = freeVmtMembers.get(vmtE164);
            } else {
                for (Map.Entry<String, TerminalService> vmt : freeVmtMembers.entrySet()) {
                    if (!vmt.getValue().isOnline())
                        continue;

                    freeTerminalService = vmt.getValue();
                    break;
                }
            }

            if (null == freeTerminalService) {
                System.out.println("getVmt2Use, has get no free vmt!!!");
                return null;
            }

            usedVmtMembers.put(freeTerminalService.getE164(), freeTerminalService);
            freeVmtMembers.remove(freeTerminalService.getE164());
            return freeTerminalService;
        }
    }

    public void useVmt(TerminalService vmtService) {
        usedVmtMembers.put(vmtService.getE164(), vmtService);
    }

    public void freeVmt(String vmtE164) {
        synchronized (usedVmtMembers) {
            TerminalService terminalService = usedVmtMembers.get(vmtE164);
            if (null != terminalService) {
                freeVmtMembers.put(vmtE164, terminalService);
                usedVmtMembers.remove(vmtE164);
            }
        }
    }

    public TerminalService findUsedVmt(String vmtE164){
        return usedVmtMembers.get(vmtE164);
    }

    public TerminalService getFreeVmt() {
        synchronized (freeVmtMembers) {
            if (freeVmtMembers.isEmpty())
                return null;

            for (Map.Entry<String, TerminalService> terminalServiceEntry : freeVmtMembers.entrySet()) {
                if (!terminalServiceEntry.getValue().isOnline())
                    continue;

                return terminalServiceEntry.getValue();
            }
        }

        return null;
    }

    public TerminalService getDstInspectionTerminal(String dstMtId){
        String dstE164 = getE164(dstMtId);
        if (null == dstE164)
            return null;

        TerminalService dstTerminal = mtMembers.get(dstE164);
        if (null != dstTerminal){
            return  dstTerminal;
        }

        return getVmtMember(dstE164);
    }

    public TerminalService getDstInspectionVmtTerminal(TerminalService srcService){
        Map<String, InspectedParam> inspentedTerminals = srcService.getInspentedTerminals();
        if (null == inspentedTerminals) {
            System.out.println("getDstInspectionVmtTerminal, null == inspentedTerminals");
            return null;
        }

        for (Map.Entry<String, InspectedParam> inspectedParamEntry : inspentedTerminals.entrySet()){
            InspectedParam inspectedParam = inspectedParamEntry.getValue();
            if (inspectedParam.isVmt()) {
                return getVmtMember(inspectedParamEntry.getKey());
            }
        }

        return null;
    }

    public TerminalService getSrcInspectionTerminal(TerminalService dstTerminal){
        if (null == dstTerminal) {
            System.out.println("getSrcInspectionTerminal, null == dstTerminal");
            return null;
        }

        TerminalService srcTerminal;
        InspectionSrcParam inspectionSrcParam = dstTerminal.getInspectionParam();
        if (null == inspectionSrcParam)
            return null;

        String inspectE164 = inspectionSrcParam.getMtE164();
        if (dstTerminal.isVmt()){
            //虚拟终端只会选看会议终端，不会选看虚拟终端
            srcTerminal = mtMembers.get(inspectE164);
        }else {
            //会议终端可以选看虚拟终端，也可以选看会议终端
            srcTerminal = usedVmtMembers.get(inspectE164);
            if (null == srcTerminal){
                srcTerminal = freeVmtMembers.get(inspectE164);
                if (null == srcTerminal){
                    srcTerminal = mtMembers.get(inspectE164);
                }
            }
        }

        return srcTerminal;
    }

    public boolean reachMaxJoinMts() {
        return ((freeVmtMembers.size() + usedVmtMembers.size() + mtMembers.size() + 1) == 191);
    }

    public int getVmtMemberNum(){
        return (freeVmtMembers.size() + usedVmtMembers.size());
    }

    public int getMtMemberNum(){
        return mtMembers.size();
    }

    public int getFreeVmtMemberNum(){
        return freeVmtMembers.size();
    }
    public void cancelGroup(){
        this.broadcastVmtService = null;
        this.mtMembers = null;
        this.freeVmtMembers = null;
        this.usedVmtMembers = null;
        this.waitDealTask = null;
        this.mtIdMap = null;
    }

    private String groupId;
    private String confId;
    private int broadcastType;   //0-未知，1-终端，2-其他
    private String broadcastMtE164;
    private TerminalService broadcastVmtService;
    private ConcurrentHashMap<String, TerminalService> freeVmtMembers;   //E164号为key
    private ConcurrentHashMap<String, TerminalService> usedVmtMembers;   //E164号为key
    private ConcurrentHashMap<String, TerminalService> mtMembers;        //E164号为key
    private Map<String, BaseRequestMsg<? extends BaseResponseMsg>> waitDealTask; //E164号为key
    private ConcurrentHashMap<String, String> mtIdMap;
}
