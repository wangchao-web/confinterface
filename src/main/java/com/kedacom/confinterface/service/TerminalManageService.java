package com.kedacom.confinterface.service;

import com.kedacom.confadapter.IConferenceManager;
import com.kedacom.confadapter.ILocalConferenceParticipant;
import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.dto.MediaResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TerminalManageService {

    public TerminalManageService(){
        super();
        this.freeVmtServiceMap = new ConcurrentHashMap<>();
        this.usedVmtServiceMap = new ConcurrentHashMap<>();
    }

    public abstract TerminalService createTerminal(String e164, boolean bVmt);

    public abstract void StartUp();

    public TerminalService getFreeVmt(){
        synchronized (freeVmtServiceMap) {
            if (freeVmtServiceMap.isEmpty())
                return null;

            for (Map.Entry<String, TerminalService> terminalServiceEntry : freeVmtServiceMap.entrySet()) {
                TerminalService terminalService = terminalServiceEntry.getValue();
                freeVmtServiceMap.remove(terminalServiceEntry.getKey());
                usedVmtServiceMap.put(terminalService.getE164(), terminalService);

                return terminalService;
            }

            return null;
        }
    }

    public List<TerminalService> getFreeVmts(int vmtNum){
        synchronized (freeVmtServiceMap) {
            if (freeVmtServiceMap.isEmpty() || freeVmtServiceMap.size() < vmtNum)
                return null;

            int chooseVmtNum = 0;
            List<TerminalService> terminals = new ArrayList<>();
            for (Map.Entry<String, TerminalService> terminalServiceEntry : freeVmtServiceMap.entrySet()) {
                TerminalService terminalService = terminalServiceEntry.getValue();
                freeVmtServiceMap.remove(terminalServiceEntry.getKey());
                usedVmtServiceMap.put(terminalService.getE164(), terminalService);
                terminals.add(terminalService);

                chooseVmtNum++;
                if (chooseVmtNum == vmtNum)
                    break;
            }

            return terminals;
        }
    }

    public TerminalService getVmt(String e164){
        synchronized (this){
            TerminalService terminalService = freeVmtServiceMap.get(e164);
            if (null != terminalService) {
                freeVmtServiceMap.remove(e164);
                usedVmtServiceMap.put(e164, terminalService);
                return terminalService;
            }
            return null;
        }
    }

    public TerminalService findVmt(String e164){
        synchronized (this){
            TerminalService terminalService = freeVmtServiceMap.get(e164);
            if (null != terminalService) {
                return terminalService;
            }

            terminalService = usedVmtServiceMap.get(e164);
            return terminalService;
        }
    }

    public List<TerminalService> queryAllUsedVmts(){
        if (usedVmtServiceMap.isEmpty())
            return null;

        List<TerminalService> terminalServices = new ArrayList<>();
        for(Map.Entry<String, TerminalService> terminalServiceEntry : usedVmtServiceMap.entrySet()){
            terminalServices.add(terminalServiceEntry.getValue());
        }

        return terminalServices;
    }

    public void freeVmt(String e164){
        synchronized (usedVmtServiceMap){
            TerminalService vmtService = usedVmtServiceMap.get(e164);
            if (null == vmtService)
                return;

            usedVmtServiceMap.remove(e164);
            freeVmtServiceMap.put(e164, vmtService);
        }
    }

    public  void freeVmts(List<Terminal> vmts){
        synchronized (usedVmtServiceMap){
            for(Terminal vmt : vmts){
                TerminalService vmtService = usedVmtServiceMap.get(vmt.getMtE164());
                if (null == vmtService)
                    return;

                usedVmtServiceMap.remove(vmt.getMtE164());
                freeVmtServiceMap.put(vmt.getMtE164(), vmtService);
            }
        }
    }

    public void setConferenceManage(IConferenceManager conferenceManage){
        this.conferenceManager = conferenceManage;
    }

    public ConfInterfaceService getConfInterfaceService() {
        return confInterfaceService;
    }

    public void setConfInterfaceService(ConfInterfaceService confInterfaceService) {
        this.confInterfaceService = confInterfaceService;
    }

    public IConferenceManager getConferenceManager() {
        return conferenceManager;
    }

    public void createConfParticipant(TerminalService terminalService){
        if (null == conferenceManager)
            return;

        if (terminalService.isVmt()) {
            ILocalConferenceParticipant conferenceParticipant = conferenceManager.CreateLocalParticipant(terminalService.getE164());
            terminalService.setConferenceParticipant(conferenceParticipant);
        } else {
            terminalService.setConferenceParticipant(null);
        }
    }

    public static void publishStatus(String account, String groupId, int status, List<MediaResource> forwardResources, List<MediaResource> reverseResources){
        if (null == confInterfacePublishService){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"TerminalManagerService, 1, publishStatus, confInterfacePublishService is null **************");
            System.out.println("TerminalManagerService, 1, publishStatus, confInterfacePublishService is null **************");
            return;
        } else {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"TerminalManagerService, 1, publishStatus, confInterfacePublishService is not null **************");
            System.out.println("TerminalManagerService, 1, publishStatus, confInterfacePublishService is not null **************");
        }
        confInterfacePublishService.publishStatus(account, groupId, status, forwardResources, reverseResources);
    }

    public static void publishStatus(String account, String groupId, int status){
        if (null == confInterfacePublishService){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"TerminalManagerService, 2, publishStatus, confInterfacePublishService is null **************");
            System.out.println("TerminalManagerService, 2, publishStatus, confInterfacePublishService is null **************");
            return;
        } else {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"TerminalManagerService, 2, publishStatus, confInterfacePublishService is not null **************");
            System.out.println("TerminalManagerService, 2, publishStatus, confInterfacePublishService is not null **************");
        }

        confInterfacePublishService.publishStatus(account, groupId, status);
    }

    public static void publishStatus(String account, String groupId, int status , int faileCode){
        if (null == confInterfacePublishService){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"TerminalManagerService, account : " + account + "2, publishStatus, confInterfacePublishService is null **************");
            System.out.println("TerminalManagerService, account : " + account + "2, publishStatus, confInterfacePublishService is null **************");
            return;
        } else {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"TerminalManagerService, account : " + account + " 2, publishStatus, confInterfacePublishService is not null **************");
            System.out.println("TerminalManagerService, account : " + account + " 2, publishStatus, confInterfacePublishService is not null **************");
        }

        confInterfacePublishService.publishStatus(account, groupId, status,faileCode);
    }

    public static void setPublishService(ConfInterfacePublishService inConfInterfacePublishService){
        confInterfacePublishService = inConfInterfacePublishService;
    }

    protected IConferenceManager conferenceManager;
    protected Map<String, TerminalService> freeVmtServiceMap;
    protected Map<String, TerminalService> usedVmtServiceMap;
    protected ConfInterfaceService confInterfaceService;

    private static ConfInterfacePublishService confInterfacePublishService;
}
