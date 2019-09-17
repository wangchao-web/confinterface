package com.kedacom.confinterface.service;

import com.kedacom.confadapter.*;
import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dao.BroadcastSrcMediaInfo;
import com.kedacom.confinterface.dao.InspectionSrcParam;
import com.kedacom.confinterface.dao.Terminal;
import com.kedacom.confinterface.dto.*;
import com.kedacom.confinterface.h323.H323TerminalManageService;
import com.kedacom.confinterface.inner.*;
import com.kedacom.confinterface.restclient.McuRestClientService;
import com.kedacom.confinterface.restclient.McuSdkClientService;
import com.kedacom.confinterface.restclient.mcu.*;
import com.kedacom.confinterface.syssetting.BaseSysConfig;
import com.kedacom.confinterface.util.ProtocalTypeEnum;
import com.kedacom.mcuadapter.IMcuClientManager;
import com.kedacom.mcuadapter.McuClientManagerFactory;
import com.kedacom.mcuadapter.McuClientManagerTypeEnum;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class ConfInterfaceInitializingService implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "now in ConfInterfaceInitializingService, protocalType:" + baseSysConfig.getProtocalType());
        System.out.println("now in ConfInterfaceInitializingService, protocalType:" + baseSysConfig.getProtocalType());

        createConferenceManage();
        registerVmts();
        if ("sdk".equals(baseSysConfig.getMcuMode())) {
            createAndInitMcuSdkClientManage();
            while (true) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "sdk登陆");
                System.out.println("sdk登陆");
                boolean bOk = mcuSdkClientService.login();
                if (bOk)
                    break;

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ConfInterfaceInitializingService ..... currentTime : " + System.currentTimeMillis());
                    System.out.println("ConfInterfaceInitializingService ..... currentTime : " + System.currentTimeMillis());
                }
            }
        } else if ("mcu".equals(baseSysConfig.getMcuMode())) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "5.2mcu登陆");
            System.out.println("5.2mcu登陆");
            loginMcuRestSrv();
        } else {
            InetAddress ia = InetAddress.getLocalHost();
            System.out.println("getMACAddress(ia)" + getMACAddress(ia));
            if (!"a4:bf:01:1c:68:c3".equalsIgnoreCase(getMACAddress(ia))) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "登陆失败");
                System.out.println("登陆失败");
                return;
            }
            System.out.println("登陆成功");
        }

        /*if("p2p".equals(baseSysConfig.getMcuMode())){
            while (true){
                if ("172.16.0.40".equals(baseSysConfig.getLocalIp())){
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"登陆");
                    System.out.println("登陆");
                    break;
                }
                try{
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e){
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"ConfInterfaceInitializingService ..... currentTime : " + System.currentTimeMillis());
                    System.out.println("ConfInterfaceInitializingService ..... currentTime : " + System.currentTimeMillis());
                }
            }
        }*/

        Map<String, String> groups = confInterfaceService.getGroups();
        if (null == groups || groups.isEmpty()) {
            //初始化协议栈
            initConfAdapter();
            //启动终端注册Gk
            terminalManageService.StartUp();

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "no groups in db, init OK! current time : " + System.currentTimeMillis());
            System.out.println("no groups in db, init OK! current time : " + System.currentTimeMillis());
            return;
        }

        for (Map.Entry<String, String> groupConf : groups.entrySet()) {
            //获取所有组的媒体资源信息
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "ConfInterfaceInitializingService, groupId : " + groupConf.getKey() + ", confId : " + groupConf.getValue());
            System.out.println("ConfInterfaceInitializingService, groupId : " + groupConf.getKey() + ", confId : " + groupConf.getValue());
            String groupId = groupConf.getKey();
            String confId = groupConf.getValue();

            //在会议接入微服务异常重启后，只需要将内存中的数据加载到内存并构造好GroupConfInfo信息，mcu会自动呼叫vmt上线，会议终端
            //不会因为会议接入微服务的重启而掉线，也不需要重新呼叫入会
            List<Terminal> confMtMembers = confInterfaceService.getGroupMtMembers(groupId);
            if (null == confMtMembers) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "groupId(" + groupId + ") has no terminal!!");
                System.out.println("groupId(" + groupId + ") has no terminal!!");
                continue;
            }

            mcuRestClientService.subscribeConfInfo(confId);
            mcuRestClientService.subscribeInspection(confId);
            mcuRestClientService.subscribeSpeaker(confId);
            mcuRestClientService.subscribeDual(confId);

            GroupConfInfo groupConfInfo = new GroupConfInfo(groupId, confId);
            loadMtInfo(groupConfInfo, confMtMembers);

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "start getGroupVmtMembers..................");
            System.out.println("start getGroupVmtMembers..................");
            int joinConfVmtNum = 0;
            List<Terminal> confVmtMembers = confInterfaceService.getGroupVmtMembers(groupId);
            if (null == confVmtMembers) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "groupId(" + groupId + ") has no virtual terminal!!");
                System.out.println("groupId(" + groupId + ") has no virtual terminal!!");
                joinConfVmtNum = confMtMembers.size();
            } else {
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
            confInterfaceService.addGroupConfInfo(groupConfInfo);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "groupId : " + groupId + ", wait vmts online! joinConfVmtNum : " + joinConfVmtNum);
            System.out.println("groupId : " + groupId + ", wait vmts online! joinConfVmtNum : " + joinConfVmtNum);
        }

        //初始化协议栈
        initConfAdapter();
        //启动终端注册Gk
        terminalManageService.StartUp();
    }

    private void createConferenceManage() {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "now in createConferenceManage.............");
        System.out.println("now in createConferenceManage.............");
        IConferenceManager conferenceManager;
        if (baseSysConfig.getProtocalType().equals(ProtocalTypeEnum.H323.getName())) {
            conferenceManager = ConferenceManagerFactory.CreateConferenceManager(ConferenceProtoEnum.CONF_H323);
        } else {
            conferenceManager = ConferenceManagerFactory.CreateConferenceManager(ConferenceProtoEnum.CONF_SIP);
        }

        terminalManageService.setConferenceManage(conferenceManager);
    }

    private void initConfAdapter() {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "now in initConfAdapter................");
        System.out.println("now in initConfAdapter................");
        IConferenceAdapterController conferenceAdapterController = terminalManageService.getConferenceManager().CreateAdapterController();
        conferenceAdapterController.SetEventHandler((H323TerminalManageService) terminalManageService);
        boolean bInitOk = conferenceAdapterController.Init("");
        if (bInitOk) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "init conferenceAdapterController successfully");
            System.out.println("init conferenceAdapterController successfully");
        } else {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "init conferenceAdapterController fail");
            System.out.println("init conferenceAdapterController fail");
        }
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
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "first start conf interface service.................");
                System.out.println("first start conf interface service.................");
                GenerateE164Service.InitE164(baseSysConfig.getE164Start());
                int maxVmts = baseSysConfig.getMaxVmts();
                for (int vmtIndex = 0; vmtIndex < maxVmts; vmtIndex++) {
                    String vmtE164 = GenerateE164Service.generateE164();
                    confInterfaceService.addVmt(vmtE164);
                }

                vmtList = confInterfaceService.getVmts();

            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "vmtList size : " + vmtList.size());
            System.out.println("vmtList size : " + vmtList.size());

            for (String vmtE164 : vmtList) {

                TerminalService vmtService = terminalManageService.createTerminal(vmtE164, true);
                vmtService.setMediaSrvIp(baseSysConfig.getMediaSrvIp());
                vmtService.setMediaSrvPort(baseSysConfig.getMediaSrvPort());

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

    private void loginMcuRestSrv() {
        while (true) {
            boolean bOk = mcuRestClientService.login();
            if (bOk)
                break;

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
        Map<String, CascadeTerminalInfo> terminalInfoMap = mcuRestClientService.getCascadesTerminal(confId, "0", true);

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
                GetConfMtInfoResponse mtInfoResponse = mcuRestClientService.getConfMtInfo(confId, mtService.getMtId());
                if (null != mtInfoResponse && mtInfoResponse.getInspection() == 1) {
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
                if (null == onlineMtsInfo) {
                    onlineMtsInfo = new OnlineMtsInfo();
                }

                OnlineMt onlineMt = new OnlineMt();
                onlineMt.setMt_id(terminalInfo.getMt_id());
                onlineMt.setForced_call(0);
                onlineMtsInfo.addOnlineMt(onlineMt);
            }

            groupConfInfo.addMtId(mtService.getMtId(), mtService.getE164());
            groupConfInfo.addMember(mtService);
        }

        if (null != onlineMtsInfo) {
            mcuRestClientService.onlineMts(confId, onlineMtsInfo);
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
            if (!mtService.isOnline())
                continue;

            InspectionSrcParam mtInspectionParam = mtService.getInspectionParam();
            if (null == mtInspectionParam)
                continue;

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
        byte [] mac =  NetworkInterface.getByName("eno1").getHardwareAddress();

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
        System.out.println("sb.toString().toUpperCase()" + sb.toString().toUpperCase());
        // 把字符串所有小写字母改为大写成为正规的mac地址并返回
        return sb.toString().toUpperCase();
    }

    @Autowired
    private BaseSysConfig baseSysConfig;

    @Autowired
    private TerminalManageService terminalManageService;

    @Autowired
    private McuRestClientService mcuRestClientService;

    @Autowired
    private ConfInterfaceService confInterfaceService;

    @Autowired
    private DefaultListableBeanFactory defaultListableBeanFactory;

    @Autowired
    private McuSdkClientService mcuSdkClientService;

    //protected final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
}
