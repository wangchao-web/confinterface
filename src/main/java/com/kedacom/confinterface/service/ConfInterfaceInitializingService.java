package com.kedacom.confinterface.service;

import com.kedacom.confadapter.*;
import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dao.BroadcastSrcMediaInfo;
import com.kedacom.confinterface.dao.InspectionSrcParam;
import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.dto.*;
import com.kedacom.confinterface.inner.*;
import com.kedacom.confinterface.restclient.McuRestClientService;
import com.kedacom.confinterface.restclient.McuRestConfig;
import com.kedacom.confinterface.restclient.McuSdkClientService;
import com.kedacom.confinterface.restclient.mcu.*;
import com.kedacom.confinterface.syssetting.AppDefaultConfig;
import com.kedacom.confinterface.syssetting.BaseSysConfig;
import com.kedacom.confinterface.util.ProtocalTypeEnum;

import com.kedacom.mcuadapter.IMcuClientManager;
import com.kedacom.mcuadapter.McuClientManagerFactory;
import com.kedacom.mcuadapter.McuClientManagerTypeEnum;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Service
public class ConfInterfaceInitializingService implements CommandLineRunner {

    @Override
    public void run(String... args) {

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "whether to use IsExternalDocking : " + BaseSysConfig.getIsExternalDocking() + baseSysConfig.isSendRecvPort());
        System.out.println("whether to use IsExternalDocking : " + BaseSysConfig.getIsExternalDocking() + baseSysConfig.isSendRecvPort());
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ConfInterfaceInitializingService confinterface version: " + VERSION);
        System.out.println("ConfInterfaceInitializingService confinterface version: " + VERSION);
        boolean status = checkDBConn();
        if (false == status) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Read configuration file fail or connect to redis failed ! End the service process !");
            System.out.println("Read configuration file fail  or connect to redis failed ! End the service process !");
            System.exit(0);
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "now in ConfInterfaceInitializingService, protocalType:" + baseSysConfig.getProtocalType());
        System.out.println("now in ConfInterfaceInitializingService, protocalType:" + baseSysConfig.getProtocalType());

        createConferenceManage();
        //初始化协议栈
        boolean bInitConfAdapter = initConfAdapter();
        if(!bInitConfAdapter){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "init conferenceAdapterController fail, end confinterface service");
            System.out.println("init conferenceAdapterController fail, end confinterface service");
            return;
        }
        registerVmts();

        //提前处理状态发布
        TerminalManageService.setPublishService(defaultListableBeanFactory.getBean(ConfInterfacePublishService.class));
        terminalManageService.setConfInterfaceService(confInterfaceService);
        if (baseSysConfig.isUseMcu()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, " loginMcuSrv *************** ");
            System.out.println(" loginMcuSrv ***************");
            loginMcuSrv();
        }

        Map<String, String> groups = confInterfaceService.getGroups();
        Map<String, String> publishUrls = confInterfaceService.getPublishUrl();
        if (publishUrls != null && !publishUrls.isEmpty()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "publishUrls is not null and not empty ******");
            System.out.println("publishUrls is not null and not empty ******");
            for (Map.Entry<String, String> publishUrl : publishUrls.entrySet()) {
                String groupId = publishUrl.getKey();
                confInterfacePublishService.addSubscribeMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS.getType(), groupId, publishUrl.getValue());
                List<String> p2PMtMembers = confInterfaceService.getP2PMtMembers(groupId);
                List<String> p2PVmtMembers = confInterfaceService.getP2PVmtMembers(groupId);

                if (p2PVmtMembers != null && !p2PVmtMembers.isEmpty()) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "p2PVmtMembers is not null and not p2PVmtMembers  : " + p2PVmtMembers.toString());
                    System.out.println("p2PVmtMembers is not null and not empty p2PVmtMembers  : " + p2PVmtMembers.toString());
                    ArrayList<String> resourceIds = new ArrayList<>();
                    for (String p2PVmtMembe : p2PVmtMembers) {
                        TerminalMediaResource terminalMediaResource = terminalMediaSourceService.getTerminalMediaResource(p2PVmtMembe);
                        if (terminalMediaResource != null) {
                            List<MediaResource> forwardResources = terminalMediaResource.getForwardResources();
                            List<MediaResource> reverseResources = terminalMediaResource.getReverseResources();
                            if (forwardResources != null && !forwardResources.isEmpty()) {
                                for (MediaResource forwardResource : forwardResources) {
                                    String resourceId = forwardResource.getId();
                                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "forwardResources account : " + p2PVmtMembe + ", resourceId : " + resourceId);
                                    System.out.println("forwardResources account : " + p2PVmtMembe + ", resourceId : " + resourceId);
                                    if (resourceId != null || !resourceId.isEmpty()) {
                                        resourceIds.add(resourceId);
                                    }
                                }
                            }

                            if (reverseResources != null && !reverseResources.isEmpty()) {
                                for (MediaResource reverseResource : reverseResources) {
                                    String resourceId = reverseResource.getId();
                                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "reverseResources account : " + p2PVmtMembe + ", resourceId : " + resourceId);
                                    System.out.println("reverseResources account : " + p2PVmtMembe + ", resourceId : " + resourceId);
                                    if (resourceId != null || !resourceId.isEmpty()) {
                                        resourceIds.add(resourceId);
                                    }
                                }
                            }
                            terminalMediaSourceService.delTerminalMediaResource(p2PVmtMembe);
                        }

                    }
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "remove all resouredIds :  " + resourceIds);
                    System.out.println("remove all resouredIds :  " + resourceIds);
                    if (!resourceIds.isEmpty() || resourceIds != null) {
                        confInterfaceService.removeExchange(resourceIds, groupId);
                    }
                    terminalMediaSourceService.delP2PVmtMembers(groupId);
                }

                if (p2PMtMembers != null && !p2PMtMembers.isEmpty()) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "p2PMtMembers is not null and not p2PMtMembers  : " + p2PMtMembers.toString());
                    System.out.println("p2PMtMembers is not null and not empty p2PMtMembers  : " + p2PMtMembers.toString());
                    for (String p2PMtMember : p2PMtMembers) {
                        TerminalStatusNotify terminalStatusNotify = new TerminalStatusNotify();
                        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "account : " + p2PMtMember + ", publishUrl : " + publishUrl.getValue());
                        System.out.println("account : " + p2PMtMember + ", publishUrl : " + publishUrl.getValue());
                        TerminalStatus terminalStatus = new TerminalStatus(p2PMtMember, "MT", TerminalOnlineStatusEnum.OFFLINE.getCode(), null, null);
                        terminalStatusNotify.addMtStatus(terminalStatus);
                        TerminalManageService.publishStatus(SubscribeMsgTypeEnum.TERMINAL_STATUS, publishUrl.getValue(), terminalStatusNotify);
                    }
                    terminalMediaSourceService.delP2PMtMembers(groupId);
                }
            }
        }

        if (null == groups || groups.isEmpty()) {
            //启动终端注册Gk
            terminalManageService.StartUp();
            initialized = true;
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "no groups in db, init OK! current time : " + System.currentTimeMillis() + ", initialized : " + initialized);
            System.out.println("no groups in db, init OK! current time : " + System.currentTimeMillis() + ", initialized : " + initialized);
            return;
        }

        for (Map.Entry<String, String> groupConf : groups.entrySet()) {
            //获取所有组的媒体资源信息
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ConfInterfaceInitializingService, groupId : " + groupConf.getKey() + ", confId : " + groupConf.getValue());
            System.out.println("ConfInterfaceInitializingService, groupId : " + groupConf.getKey() + ", confId : " + groupConf.getValue());
            String groupId = groupConf.getKey();
            String confId = groupConf.getValue();

            GroupConfInfo groupConfInfo = new GroupConfInfo(groupId, confId);
            loadMonitorsInfo(groupConfInfo);
            Map<String, String> confCreateTypeHashs = confInterfaceService.getConfCreateTypeHashs();
            if (confCreateTypeHashs != null && !confCreateTypeHashs.isEmpty()) {
                if (confCreateTypeHashs.containsKey(confId)) {
                    groupConfInfo.setCreatedConf(confCreateTypeHashs.get(confId));
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ConfInterfaceInitializingService confCreateTypeHashs exit confId : " + confId + ", createConfType : " + confCreateTypeHashs.get(confId));
                    System.out.println("ConfInterfaceInitializingService confCreateTypeHashs exit confId : " + confId + ", createConfType : " + confCreateTypeHashs.get(confId));
                }
            }
            confInterfaceService.addGroupConfInfo(groupConfInfo);
            if ("mcu".equals(baseSysConfig.getMcuMode())) {
                mcuRestClientService.subscribeConfInfo(confId);
                mcuRestClientService.subscribeInspection(confId);
                mcuRestClientService.subscribeSpeaker(confId);
                mcuRestClientService.subscribeDual(confId);
                mcuRestClientService.subscribeConfMts(confId);
                mcuRestClientService.subscribeMixs(confId);
                mcuRestClientService.subscribeVmps(confId);
            }
            //在会议接入微服务异常重启后，只需要将内存中的数据加载到内存并构造好GroupConfInfo信息，mcu会自动呼叫vmt上线，会议终端
            //不会因为会议接入微服务的重启而掉线，也不需要重新呼叫入会
            List<Terminal> confMtMembers = confInterfaceService.getGroupMtMembers(groupId);
            if (null == confMtMembers) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "groupId(" + groupId + ") has no terminal!!");
                System.out.println("groupId(" + groupId + ") has no terminal!!");
                continue;
            }

            loadMtInfo(groupConfInfo, confMtMembers);

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "start getGroupVmtMembers..................");
            System.out.println("start getGroupVmtMembers..................");
            int joinConfVmtNum = 0;
            List<Terminal> confVmtMembers = confInterfaceService.getGroupVmtMembers(groupId);
            if (null == confVmtMembers) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "groupId(" + groupId + ") has no virtual terminal!!");
                System.out.println("groupId(" + groupId + ") has no virtual terminal!!");
                joinConfVmtNum = confMtMembers.size();
            } else if ("mcu".equals(baseSysConfig.getMcuMode())) {
                //订阅设备上线信息
                mcuRestClientService.subscribeConfMts(confId);
                loadVmtInfo(groupConfInfo, confVmtMembers);
            }

            BroadcastSrcMediaInfo broadcastSrcMediaInfo = confInterfaceService.getBroadcastSrc(groupId);
            if (null != broadcastSrcMediaInfo) {
                loadBroadcastInfo(groupConfInfo, broadcastSrcMediaInfo);
            } else {
                groupConfInfo.setBroadcastVmtService(null);
            }

            //构造被选看信息
            constructInspectedParam(groupConfInfo);
            groupConfInfo.setCreatedConf("confinterface");
            confInterfaceService.addGroupConfInfo(groupConfInfo);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "groupId : " + groupId + ", wait vmts online! joinConfVmtNum : " + joinConfVmtNum);
            System.out.println("groupId : " + groupId + ", wait vmts online! joinConfVmtNum : " + joinConfVmtNum);
        }

        //启动终端注册Gk
        terminalManageService.StartUp();
        initialized = true;
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "no groups in db, init OK! current time : " + System.currentTimeMillis() + ", initialized : " + initialized);
        System.out.println("no groups in db, init OK! current time : " + System.currentTimeMillis() + ", initialized : " + initialized);
    }

    private void createConferenceManage() {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "now in createConferenceManage.............");
        System.out.println("now in createConferenceManage.............");
        IConferenceManager conferenceManager;

        if (baseSysConfig.getProtocalType().equals(ProtocalTypeEnum.H323.getName())) {
            conferenceManager = ConferenceManagerFactory.CreateConferenceManager(ConferenceProtoEnum.CONF_H323);
        }
        else if(baseSysConfig.getProtocalType().equals(ProtocalTypeEnum.SIP.getName())) {
            conferenceManager = ConferenceManagerFactory.CreateConferenceManager(ConferenceProtoEnum.CONF_SIP);
        }else{
            conferenceManager = ConferenceManagerFactory.CreateConferenceManager(ConferenceProtoEnum.CONF_H323);
        }

        terminalManageService.setConferenceManage(conferenceManager);
    }

    private boolean initConfAdapter() {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "now in initConfAdapter................");
        System.out.println("now in initConfAdapter................");
        IConferenceAdapterController conferenceAdapterController = terminalManageService.getConferenceManager().CreateAdapterController();
        conferenceAdapterController.SetEventHandler((IConferenceEventHandler) terminalManageService);

        boolean bInitOk = conferenceAdapterController.Init("");
        if (bInitOk) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "init conferenceAdapterController successfully");
            System.out.println("init conferenceAdapterController successfully");
        } else {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "init conferenceAdapterController fail");
            System.out.println("init conferenceAdapterController fail");
        }
        return bInitOk;
    }

    private void registerVmts() {
        try {

            //查询数据库中是否已经存在vmt信息，如果存在，则直接使用Vmt中数据重新构建
            List<String> vmtList = confInterfaceService.getVmts();

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "++++++++++++++baseSysSetting+++++++++++++\r\n" + baseSysConfig.toString());
            System.out.println("++++++++++++++baseSysSetting+++++++++++++\r\n" + baseSysConfig.toString());
            if (null == vmtList || vmtList.isEmpty()) {
                //会议接入服务第一次启动，根据配置文件中的起始E164号及虚拟终端数量
                //生成E164号
                if (null != baseSysConfig.getE164Start()) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "first start conf interface service.................");
                    System.out.println("first start conf interface service.................");
                    GenerateE164Service.InitE164(baseSysConfig.getE164Start());
                    int maxVmts = baseSysConfig.getMaxVmts();
                    for (int vmtIndex = 0; vmtIndex < maxVmts; vmtIndex++) {
                        String vmtE164 = GenerateE164Service.generateE164();
                        confInterfaceService.addVmt(vmtE164);
                    }
                } else if (null != baseSysConfig.getProxyMTs()) {
                    ConcurrentHashMap<String, String> proxyMts = baseSysConfig.getMapProxyMTs();
                    for (Map.Entry<String, String> proxyMt : proxyMts.entrySet()) {
                        confInterfaceService.addVmt(proxyMt.getKey());
                    }
                }

                vmtList = confInterfaceService.getVmts();

            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "vmtList size : " + vmtList.size());
            System.out.println("vmtList size : " + vmtList.size() + ", localIp :" + baseSysConfig.getLocalIp());

            TerminalService.initScheduleP2PCallUrl(baseSysConfig.getScheduleSrvHttpAddr());
            TerminalService.initNotifyUrl(baseSysConfig.getLocalIp(), appDefaultConfig.getServerPort());
            TerminalService.setMediaSrvIp(baseSysConfig.getMediaSrvIp());
            TerminalService.setMediaSrvPort(baseSysConfig.getMediaSrvPort());
            TerminalService.setLocalIp(baseSysConfig.getLocalIp());
            TerminalService.setLocalPort(appDefaultConfig.getServerPort());

            ConcurrentHashMap<String, String> proxyMts = baseSysConfig.getMapProxyMTs();
            for (String vmtE164 : vmtList) {
                TerminalService vmtService = terminalManageService.createTerminal(vmtE164, true);
                if(baseSysConfig.isUseDeviceId()){
                    String deviceID = constructorDeviceID(vmtE164);
                    vmtService.setDeviceID(deviceID);
                }

                if (null != proxyMts && proxyMts.containsKey(vmtE164)) {
                    vmtService.setProxyMTE164(proxyMts.get(vmtE164));
                }

                defaultListableBeanFactory.registerSingleton(vmtE164, vmtService);
                defaultListableBeanFactory.autowireBean(vmtService);

                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "registerSingleton, vmtE164 : " + vmtE164);
                System.out.println("registerSingleton, vmtE164 : " + vmtE164);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createAndInitMcuSdkClientManage() {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "now in createMcuSdkClientManage.............");
        System.out.println("now in createMcuSdkClientManage.............");
        try {
            IMcuClientManager mcuClientManager = McuClientManagerFactory.createManager(McuClientManagerTypeEnum.KD_MCU_MCS_SDK);

            mcuSdkClientService.setMcuClientManager(mcuClientManager);
            mcuSdkClientService.initMcuSdk();

        } catch (Exception e) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "createManager failed!");
            System.out.println("createManager failed!");
            e.printStackTrace();
        }
    }

    private void loginMcuSrv() {
        String mcuMode = baseSysConfig.getMcuMode();

        if ("sdk".equals(mcuMode)) {
            createAndInitMcuSdkClientManage();
        }

        boolean bOk = false;
        while (true) {
            if ("mcu".equals(mcuMode)) {
                bOk = mcuRestClientService.login();
            } else if ("sdk".equals(mcuMode)) {
                bOk = mcuSdkClientService.login();
            }

            if (bOk) {
                break;
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ConfInterfaceInitializingService ..... currentTime : " + System.currentTimeMillis());
                System.out.println("ConfInterfaceInitializingService ..... currentTime : " + System.currentTimeMillis());
            }
        }
    }

    private void loadMtInfo(GroupConfInfo groupConfInfo, List<Terminal> confMtMembers) {
        String groupId = groupConfInfo.getGroupId();
        String confId = groupConfInfo.getConfId();
        Map<String, CascadeTerminalInfo> terminalInfoMap = null;

        if (null != mcuRestClientService) {
            terminalInfoMap = mcuRestClientService.getCascadesTerminal(confId, "0", true);
        } else if (null != mcuSdkClientService) {
            //todo:增加 mcuSdkClientService的相关处理
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "groupId(" + groupId + ") has " + confMtMembers.size() + " terminals in db!");
        System.out.println("groupId(" + groupId + ") has " + confMtMembers.size() + " terminals in db!");
        Iterator<Terminal> iterator = confMtMembers.iterator();
        OnlineMtsInfo onlineMtsInfo = null;

        while (iterator.hasNext()) {
            Terminal terminal = iterator.next();
            String mtE164 = terminal.getMtE164();
            CascadeTerminalInfo terminalInfo = null;
            if (null != terminalInfoMap) {
                terminalInfo = terminalInfoMap.get(mtE164);
            }

            if (null == terminalInfo) {
                //在会议中没有找到相应的会议终端，有可能是会议终端在微服务重启过程中退出了会议
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "not find mt(" + mtE164 + ") in conf, del from db!");
                System.out.println("not find mt(" + mtE164 + ") in conf, del from db!");
                confInterfaceService.delGroupMtMember(groupId, terminal);
                confInterfaceService.delGroupInspectionParam(mtE164);
                iterator.remove();
                continue;
            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "groupId : " + groupId + ", mtE164 : " + mtE164);
            System.out.println("groupId : " + groupId + ", mtE164 : " + mtE164);
            TerminalService mtService = terminalManageService.createTerminal(mtE164, false);
            mtService.setGroupId(groupId);
            mtService.setConfId(confId);
            mtService.setMtId(terminalInfo.getMt_id());

            InspectionSrcParam inspectionParam = confInterfaceService.getGroupInspectionParam(mtE164);
            if (null != inspectionParam) {
                String mode = inspectionParam.getMode();
                mtService.setInspectionParam(inspectionParam);

                boolean bInspection = false;
                if (null != mcuRestClientService) {
                    GetConfMtInfoResponse mtInfoResponse = mcuRestClientService.getConfMtInfo(confId, mtService.getMtId());
                    if (null != mtInfoResponse && mtInfoResponse.getInspection() == 1) {
                        bInspection = true;
                    }
                } else if (null != mcuSdkClientService) {
                    //todo:增加 mcuSdkClientService的相关处理
                }

                if (bInspection) {
                    mtService.setInspectionStatus(InspectionStatusEnum.OK);

                    if (mode.equals(InspectionModeEnum.ALL.getName()) || mode.equals(InspectionModeEnum.VIDEO.getName())) {
                        mtService.setInspectVideoStatus(InspectionStatusEnum.OK.getCode());
                    }

                    if (mode.equals(InspectionModeEnum.ALL.getName()) || mode.equals(InspectionModeEnum.AUDIO.getName())) {
                        mtService.setInspectAudioStatus(InspectionStatusEnum.OK.getCode());
                    }
                }
            }

            if (terminalInfo.getOnline() == 1) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "mt(e164:" + mtE164 + ", mtId:" + terminalInfo.getMt_id() + ") is online in conf(" + confId + ")");
                System.out.println("mt(e164:" + mtE164 + ", mtId:" + terminalInfo.getMt_id() + ") is online in conf(" + confId + ")");
                mtService.setOnline(TerminalOnlineStatusEnum.ONLINE.getCode());
            } else {
                mtService.setOnline(TerminalOnlineStatusEnum.OFFLINE.getCode());
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "mt(" + mtService.getE164() + ") hang off during conf interface microservice reboot");
                System.out.println("mt(" + mtService.getE164() + ") hang off during conf interface microservice reboot");

                if (null != mcuRestClientService) {
                    if (null == onlineMtsInfo) {
                        onlineMtsInfo = new OnlineMtsInfo();
                    }

                    OnlineMt onlineMt = new OnlineMt();
                    onlineMt.setMt_id(terminalInfo.getMt_id());
                    onlineMt.setForced_call(0);
                    onlineMtsInfo.addOnlineMt(onlineMt);
                } else if (null != mcuSdkClientService) {
                    //todo:增加 mcuSdkClientService的相关处理
                }
            }

            groupConfInfo.addMtId(mtService.getMtId(), mtService.getE164());
            groupConfInfo.addMember(mtService);
        }

        if (null != mcuRestClientService && null != onlineMtsInfo) {
            mcuRestClientService.onlineMts(confId, onlineMtsInfo);
        } else if (null != mcuSdkClientService) {
            //todo:增加 mcuSdkClientService的相关处理
        }
    }

    private void loadMonitorsInfo(GroupConfInfo groupConfInfo) {
        String groupId = groupConfInfo.getGroupId();
        String confId = groupConfInfo.getConfId();

        ConcurrentHashMap<String, MonitorsMember> monitorsMembers = confInterfaceService.getMonitorsMembers(confId);
        if (null == monitorsMembers || monitorsMembers.isEmpty()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ConfInterfaceInitializingService loadMonitorsInfo monitorsMembers is null or empty !");
            System.out.println("ConfInterfaceInitializingService loadMonitorsInfo monitorsMembers is null or empty !");
            return;
        }
        ArrayList<String> resourceIds = new ArrayList<>();
        for (Map.Entry<String, MonitorsMember> monitorsMemberMap : monitorsMembers.entrySet()) {
            MonitorsMember monitorsMember = monitorsMemberMap.getValue();
            if (null == monitorsMember) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ConfInterfaceInitializingService monitorsMember is null or empty !");
                System.out.println("ConfInterfaceInitializingService monitorsMember is null or empty !");
                continue;
            }


            if (null != mcuRestClientService) {
                int mode = monitorsMember.getMode();
                int type = monitorsMember.getType();

                McuStatus getMonistors = mcuRestClientService.GetMonistors(confId, monitorsMember.getDstIp(), monitorsMember.getPort());
                if (getMonistors.getValue() == 200) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ConfInterfaceInitializingService getMonistors is succeed confId : " + confId + ", Ip : " + monitorsMember.getDstIp() + ", port : " + monitorsMember.getPort());
                    System.out.println("ConfInterfaceInitializingService getMonistors is succeed confId : " + confId + ", Ip : " + monitorsMember.getDstIp() + ", port : " + monitorsMember.getPort());
                    continue;
                }

                McuStartMonitorsParam mcuStartMonitorsParam = new McuStartMonitorsParam();
                mcuStartMonitorsParam.setMode(mode);
                MonitorsSrc monitorsSrc = new MonitorsSrc();
                monitorsSrc.setType(type);
                VideoFormat mcuVideoFormat = new VideoFormat();
                AudioFormat audioFormat = new AudioFormat();
                McuMonitorsDst mcuMonitorsDst = new McuMonitorsDst(monitorsMember.getDstIp(), monitorsMember.getPort());
                if (type == 1) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ConfInterfaceInitializingService loadMonitorsInfo  type is Mt mtID : " + monitorsMember.getMtId());
                    System.out.println("ConfInterfaceInitializingService loadMonitorsInfo  type is Mt mtID : " + monitorsMember.getMtId());
                    monitorsSrc.setMt_id(monitorsMember.getMtId());
                }
                monitorsSrc.setMt_id(monitorsMember.getMtId());
                if (mode == 0) {
                    String videoFormat = mcuRestConfig.getVideoFormat();
                    String[] videoformats = videoFormat.split(",");
                    for (String videoformat : videoformats) {
                        String[] result = videoformat.split("/");
                        mcuVideoFormat.setFormat(Integer.valueOf(result[0]));
                        mcuVideoFormat.setResolution(Integer.valueOf(result[1]));
                        mcuVideoFormat.setFrame(Integer.valueOf(result[2]));
                        mcuVideoFormat.setBitrate(Integer.valueOf(result[3]));
                    }
                }
                if (mode == 1) {
                    audioFormat.setFormat(4);
                    audioFormat.setChn_num(2);
                }
                mcuStartMonitorsParam.setSrc(monitorsSrc);
                mcuStartMonitorsParam.setVideo_format(mcuVideoFormat);
                mcuStartMonitorsParam.setAudio_format(audioFormat);
                mcuStartMonitorsParam.setDst(mcuMonitorsDst);

                McuStatus mcuStatus = mcuRestClientService.startMonitors(confId, mcuStartMonitorsParam);
                if (mcuStatus.getValue() != 200) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ConfInterfaceInitializingService startMonitors is failed monitorsMember : " + monitorsMember.toString());
                    System.out.println("ConfInterfaceInitializingService startMonitors is failed monitorsMember : " + monitorsMember.toString());
                    monitorsMembers.remove(monitorsMemberMap.getKey());
                    resourceIds.add(monitorsMember.getId());
                }
            }
        }
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ConfInterfaceInitializingService loadMonitorsInfo monitorsMembers " + monitorsMembers.toString());
        System.out.println("ConfInterfaceInitializingService loadMonitorsInfo monitorsMembers " + monitorsMembers.toString());
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ConfInterfaceInitializingService loadMonitorsInfo resourceIds  " + resourceIds.toString());
        System.out.println("ConfInterfaceInitializingService loadMonitorsInfo resourceIds  " + resourceIds.toString());
        groupConfInfo.setMonitorsMembers(monitorsMembers);
        terminalMediaSourceService.setMonitorsMembers(confId, monitorsMembers);
        ConfInterfaceService.monitorsMemberHearbeat.put(confId, monitorsMembers);
        if (!resourceIds.isEmpty() || null != resourceIds) {
            confInterfaceService.removeExchange(resourceIds, groupId);
        }

    }

    private void loadVmtInfo(GroupConfInfo groupConfInfo, List<Terminal> confVmtMembers) {
        String groupId = groupConfInfo.getGroupId();
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "loadVmtInfo, group(" + groupConfInfo.getGroupId() + ") has " + confVmtMembers.size() + " vmts!");
        System.out.println("loadVmtInfo, group(" + groupConfInfo.getGroupId() + ") has " + confVmtMembers.size() + " vmts!");
        for (Terminal vmt : confVmtMembers) {
            String vmtE164 = vmt.getMtE164();
            TerminalService vmtService = terminalManageService.getVmt(vmtE164);
            InspectionSrcParam vmtInspectionParam = confInterfaceService.getGroupInspectionParam(vmtE164);
            if (null != vmtInspectionParam) {
                vmtService.setInspectionParam(vmtInspectionParam);
            }

            TerminalMediaResource terminalMediaResource = confInterfaceService.getTerminalMediaResource(vmtE164);
            if (null != terminalMediaResource) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "loadVmtInfo, vmt(" + vmtE164 + ") has terminal media resource!");
                System.out.println("loadVmtInfo, vmt(" + vmtE164 + ") has terminal media resource!");
                List<MediaResource> forwardResources = terminalMediaResource.getForwardResources();
                if (null != forwardResources) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "loadVmtInfo, forwardResource:" + forwardResources.size());
                    System.out.println("loadVmtInfo, forwardResource:" + forwardResources.size());
                    for (MediaResource mediaResource : forwardResources) {
                        DetailMediaResouce detailMediaResouce = new DetailMediaResouce(mediaResource);
                        vmtService.addForwardChannel(detailMediaResouce);
                    }
                }

                List<MediaResource> reverseResources = terminalMediaResource.getReverseResources();
                if (null != reverseResources) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "loadVmtInfo, reverseResource:" + reverseResources.size());
                    System.out.println("loadVmtInfo, reverseResource:" + reverseResources.size());
                    for (MediaResource mediaResource : reverseResources) {
                        DetailMediaResouce detailMediaResouce = new DetailMediaResouce(mediaResource);
                        vmtService.addReverseChannel(detailMediaResouce);
                    }
                }
            }

            vmtService.setGroupId(groupId);
            groupConfInfo.addMember(vmtService);
        }
    }

    private void loadBroadcastInfo(GroupConfInfo groupConfInfo, BroadcastSrcMediaInfo broadcastSrcMediaInfo) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "group(" + groupConfInfo.getGroupId() + ") exist broadcast src! broadcastInfo:" + broadcastSrcMediaInfo);
        System.out.println("group(" + groupConfInfo.getGroupId() + ") exist broadcast src! broadcastInfo:" + broadcastSrcMediaInfo);
        TerminalService broadcastService = groupConfInfo.getVmtMember(broadcastSrcMediaInfo.getVmtE164());
        groupConfInfo.setBroadcastType(broadcastSrcMediaInfo.getType());
        groupConfInfo.setBroadcastMtE164(broadcastSrcMediaInfo.getMtE164());
        groupConfInfo.setBroadcastVmtService(broadcastService);
        groupConfInfo.delMember(broadcastService);
    }

    private void constructInspectedParam(GroupConfInfo groupConfInfo) {
        Map<String, TerminalService> mtMembers = groupConfInfo.getMtMembers();

        for (Map.Entry<String, TerminalService> mtMember : mtMembers.entrySet()) {
            TerminalService mtService = mtMember.getValue();
            if (!mtService.isOnline()) {
                continue;
            }

            InspectionSrcParam mtInspectionParam = mtService.getInspectionParam();
            if (null == mtInspectionParam) {
                continue;
            }

            TerminalService vmtService = groupConfInfo.getVmtMember(mtInspectionParam.getMtE164());
            if (null != vmtService) {
                //构建被选看
                InspectedParam inspectedParam = new InspectedParam();
                inspectedParam.setVmt(false);
                vmtService.addInspentedTerminal(mtService.getE164(), inspectedParam);
                continue;
            }

            TerminalService srcMtService = mtMembers.get(mtInspectionParam.getMtE164());
            if (null != srcMtService) {
                InspectedParam inspectedParam = new InspectedParam();
                inspectedParam.setVmt(true);
                if (mtService.isInspection()) {
                    inspectedParam.setStatus(InspectionStatusEnum.OK);
                }
                srcMtService.addInspentedTerminal(mtService.getE164(), inspectedParam);
                srcMtService.setInspectedStatus(InspectionStatusEnum.OK);
            }
        }
    }

    private static String getMACAddress(InetAddress ia) throws Exception {
        // 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
        byte[] mac = NetworkInterface.getByName("eno1").getHardwareAddress();

        // 下面代码是把mac地址拼装成String
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append(":");
            }
            // mac[i] & 0xFF 是为了把byte转化为正整数
            String s = Integer.toHexString(mac[i] & 0xFF);
            sb.append(s.length() == 1 ? 0 + s : s);
        }
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "sb.toString().toUpperCase()" + sb.toString().toUpperCase());
        System.out.println("sb.toString().toUpperCase()" + sb.toString().toUpperCase());
        // 把字符串所有小写字母改为大写成为正规的mac地址并返回
        return sb.toString().toUpperCase();
    }

    private String constructorDeviceID(String E164) {
        String deviceID = "";
        StrBuilder strBuilder = new StrBuilder();
        strBuilder.append(baseSysConfig.getMediaSrvIp());
        strBuilder.append(":");
        strBuilder.append(baseSysConfig.getMediaSrvPort());
        strBuilder.append(":");
        strBuilder.append(E164);
        int code = strBuilder.hashCode();
        String value = String.valueOf(code);
        deviceID = value.replaceAll("-", "0");
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "constructorDeviceID, strBuilder : " + strBuilder.toString() + ", code : " + code + ", deviceID : " + deviceID);
        System.out.println("constructorDeviceID, strBuilder : " + strBuilder.toString() + ", code : " + code + ", deviceID : " + deviceID);
        if (deviceID.length() == 8) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "deviceID length equal to 8 constructorDeviceID, deviceID : " + deviceID);
            System.out.println("deviceID length equal to 8 constructorDeviceID, deviceID : " + deviceID);
            return deviceID;
        }
        if (deviceID.length() > 8) {
            deviceID = deviceID.substring(0, 8);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "deviceID length greater than 8 constructorDeviceID, deviceID : " + deviceID);
            System.out.println("deviceID length greater than 8 constructorDeviceID, deviceID : " + deviceID);
            return deviceID;
        }

        deviceID = StringUtils.leftPad(deviceID, 8, "0");
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "deviceID length less than 8 constructorDeviceID, deviceID : " + deviceID);
        System.out.println("deviceID length less than 8 constructorDeviceID, deviceID : " + deviceID);
        return deviceID;
    }

    /**
     * 检查redis能否连接
     *
     * @return
     */
    private boolean checkDBConn() {

        for (int i = 0; i < 12; i++) {
            if (terminalMediaSourceService.checkDBConnStatus()) {
                return true;
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private void probeMedia() {

    }

    @Autowired
    private BaseSysConfig baseSysConfig;

    @Autowired
    private AppDefaultConfig appDefaultConfig;

    @Autowired
    private TerminalManageService terminalManageService;

    @Autowired(required = false)
    private McuRestClientService mcuRestClientService;

    @Autowired
    private ConfInterfaceService confInterfaceService;

    @Autowired
    private DefaultListableBeanFactory defaultListableBeanFactory;

    @Autowired
    private TerminalMediaSourceService terminalMediaSourceService;

    @Autowired(required = false)
    private McuSdkClientService mcuSdkClientService;

    @Autowired
    private ConfInterfacePublishService confInterfacePublishService;


    @Autowired(required = false)
    private McuRestConfig mcuRestConfig;

    //版本号修复
    public static final String VERSION = "confinterface-V.1.2.0.7";

    public static Boolean initialized = false;

    //protected final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

}
