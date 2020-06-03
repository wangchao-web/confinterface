package com.kedacom.confinterface.service;

import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dto.BaseResponseMsg;
import com.kedacom.confinterface.dto.MediaResource;
import com.kedacom.confinterface.dto.TerminalStatus;
import com.kedacom.confinterface.dto.TerminalStatusNotify;
import com.kedacom.confinterface.inner.SubscribeMsgTypeEnum;
import com.kedacom.confinterface.inner.TerminalOnlineStatusEnum;
import com.kedacom.confinterface.restclient.McuRestClientService;
import com.kedacom.confinterface.restclient.RestClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ConditionalOnProperty(name = "confinterface.sys.pushServiceType", havingValue = "mediaSchedule", matchIfMissing = true)
@Service
@EnableScheduling
public class SchedulePublishService extends ConfInterfacePublishService {


    @Override
    public void publishStatus(String account, String groupId, int status) {
        publishStatus(account, groupId, status, null, null);
    }

    @Override
    public void publishStatus(String account, String groupId, int status, int faileCode) {
        TerminalStatusNotify terminalStatusNotify = new TerminalStatusNotify();
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "account : " + account + " publishStatus callFailureCode " + faileCode);
        System.out.println("account : " + account + " publishStatus callFailureCode " + faileCode);
        TerminalStatus terminalStatus = new TerminalStatus(account, "MT", status, null, null, faileCode);
        terminalStatusNotify.addMtStatus(terminalStatus);
        publishMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS, groupId, terminalStatusNotify);
    }

  //用于会议服务断链再重启之后推送状态
    @Override
    public void publishStatus(SubscribeMsgTypeEnum type, String publishUrl, Object publishMsg) {
        serviceRestartPublishMessage(type, publishUrl, publishMsg);
    }

    @Override
    public void publishStatus(String account, String groupId, int status, List<MediaResource> forwardResources, List<MediaResource> reverseResources) {
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "account : " + account + " now in confInterfacePublishService publishStatus!!");
        System.out.println("account : " + account + " now in confInterfacePublishService publishStatus!!");
        String accountType = "MT";

        if (account.equals(groupId)) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "publishStatus, type: Conference!");
            System.out.println("publishStatus, type: Conference!");
            accountType = "Conference";
            //account = "Conference";
        } else if (status == TerminalOnlineStatusEnum.DUALSTREAM.getCode()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "publishStatus, type: Dual!");
            System.out.println("publishStatus, type: Dual!");
            accountType = "Dual";
        }

        TerminalStatusNotify terminalStatusNotify = new TerminalStatusNotify();
        TerminalStatus terminalStatus = new TerminalStatus(account, accountType, status, forwardResources, reverseResources);
        terminalStatusNotify.addMtStatus(terminalStatus);
        if ("Conference".equals(accountType)) {
            Map<String, String> groupUrls = subscribeMsgs.get(SubscribeMsgTypeEnum.TERMINAL_STATUS.getType());
            if (null == groupUrls) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "publishStatus, has no client subscribe message, type :" + SubscribeMsgTypeEnum.TERMINAL_STATUS.getType() + ", name:" + SubscribeMsgTypeEnum.TERMINAL_STATUS.getName());
                System.out.println("publishStatus, has no client subscribe message, type :" + SubscribeMsgTypeEnum.TERMINAL_STATUS.getType() + ", name:" + SubscribeMsgTypeEnum.TERMINAL_STATUS.getName());
                return;
            }
            if (groupUrls.containsKey(groupId)) {
                publishMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS, groupId, terminalStatusNotify);
            } else {
                publishMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS, "groupnotify", terminalStatusNotify);
            }

        } else {
            publishMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS, groupId, terminalStatusNotify);
        }

    }

    @Override
    @Async("confTaskExecutor")
    public void addSubscribeMessage(int type, String groupId, String url) {
        synchronized (this) {
            Map<String, String> groupUrl = subscribeMsgs.get(type);
            if (null == groupUrl) {
                groupUrl = new ConcurrentHashMap<>();
                subscribeMsgs.put(type, groupUrl);
            }
            if ("groupnotify".equals(groupId)) {
                //订阅mcu上所有会议信息  放到来媒体调度来订阅的时候处理
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "addSubscribeMessage Subscribe all conf Info ");
                System.out.println("addSubscribeMessage Subscribe all conf Info ");
                mcuRestClientService.subscribeAllConfInfo();
            }
            groupUrl.put(groupId, url);
            terminalMediaSourceService.setPublishUrl(groupId, url);
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "addSubscribeMessage groupId : " + groupId + ", url " + url);
            System.out.println("addSubscribeMessage groupId : " + groupId + ", url " + url);
        }
    }

    @Override
    @Async("confTaskExecutor")
    public void cancelSubscribeMessage(int type, String groupId, String url) {
        synchronized (this) {
            Map<String, String> groupUrl = subscribeMsgs.get(type);
            if (null == groupUrl) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "cancelSubscribeMessage groupUrl is null ******");
                System.out.println("cancelSubscribeMessage groupUrl is null ******");
                return;
            }
            if (!groupUrl.containsKey(groupId)) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "cancelSubscribeMessage groupUrl is  not containKey groupId : " + groupId);
                System.out.println("cancelSubscribeMessage groupUrl is not containsKey groupId : " + groupId);
                return;
            }
            if ("groupnotify".equals(groupId)) {
                //订阅mcu上所有会议信息  放到来媒体调度来订阅的时候处理
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "cancelSubscribeMessage Subscribe all conf Info ");
                System.out.println("cancelSubscribeMessage Subscribe all conf Info ");

                List<String> channels = mcuRestClientService.getConfSubcribeChannelMap().get("allConf");
                if (channels == null || channels.isEmpty()) {
                    LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "cancelSubscribeMessage channels is null or isEmpty");
                    System.out.println("cancelSubscribeMessage channels is null or isEmpty");
                }else{
                    if (null != channels) {
                        for (String channel : channels) {
                            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "cancelSubscribeMessage channel : " + channel);
                            System.out.println("cancelSubscribeMessage channel : " + channel);
                            mcuRestClientService.removeMcuSubscribe(channel);
                        }
                    }
                    channels.clear();
                    mcuRestClientService.removeConfSubcribeChannelMap("allConf");
                }

            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "cancelSubscribeMessage groupId : " + groupId + ", url " + url);
            System.out.println("cancelSubscribeMessage groupId : " + groupId + ", url " + url);
            groupUrl.remove(groupId);
            terminalMediaSourceService.deletePublishUrl(groupId);
        }
    }

    private void publishMessage(SubscribeMsgTypeEnum type, String groupId, Object publishMsg) {
        Map<String, String> groupUrls = subscribeMsgs.get(type.getType());
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "type : " + type + " ,groupId : " + groupId + ",publishMsg : " + publishMsg.toString());
        System.out.println("type : " + type + " ,groupId : " + groupId + ",publishMsg : " + publishMsg.toString());
        if (null == groupUrls) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "publishMessage, has no client subscribe message, type :" + type.getType() + ", name:" + type.getName());
            System.out.println("publishMessage, has no client subscribe message, type :" + type.getType() + ", name:" + type.getName());
            return;
        }

        String publishUrl = groupUrls.get(groupId);
        if (null == publishUrl) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "publishMessage, has no client subscribe message(" + type.getName() + ") of group(" + groupId + ")");
            System.out.println("publishMessage, has no client subscribe message(" + type.getName() + ") of group(" + groupId + ")");
            return;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "publishMessage, groupId:" + groupId);
        System.out.println("publishMessage, groupId:" + groupId);
        ResponseEntity<BaseResponseMsg> publishResponse = restClientService.exchangeJson(publishUrl, HttpMethod.POST, publishMsg, null, BaseResponseMsg.class);
        if (publishResponse.getStatusCode().is2xxSuccessful() && publishResponse.getBody().getCode() == 0) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "publishMessage OK! type : " + type.getName() + ", publishUrl : " + publishUrl);
            System.out.println("publishMessage OK! type : " + type.getName() + ", publishUrl : " + publishUrl);
           /* TerminalStatusNotify terminalStatusNotify1 = (TerminalStatusNotify) publishMsg;
            if (terminalStatusNotify1.getMtStatusNotify().get(0).getStatus() == 1 && terminalStatusNotify1.getMtStatusNotify().get(0).getCallMode().equals("p2p")) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"terminalStatusNotify1  DeviceId : " + terminalStatusNotify1.getMtStatusNotify().get(0).getDeviceId() + ", terminalStatusNotify1 Status :" + terminalStatusNotify1.getMtStatusNotify().get(0).getStatus() +", terminalStatusNotify1 callMode : "+terminalStatusNotify1.getMtStatusNotify().get(0).getCallMode());
                System.out.println("terminalStatusNotify1  DeviceId : " + terminalStatusNotify1.getMtStatusNotify().get(0).getDeviceId() + ", terminalStatusNotify1 Status :" + terminalStatusNotify1.getMtStatusNotify().get(0).getStatus() +", terminalStatusNotify1 callMode : "+terminalStatusNotify1.getMtStatusNotify().get(0).getCallMode());
                terminalMediaSourceService.setMtPublish(terminalStatusNotify1.getMtStatusNotify().get(0).getDeviceId(), publishUrl);
            }*/
            return;
        }

        if (!publishFail.containsKey(publishUrl)){
            publishFail.put(publishUrl, publishMsg);
        } else {
            if (type == SubscribeMsgTypeEnum.TERMINAL_STATUS) {
                TerminalStatusNotify terminalStatusNotify = (TerminalStatusNotify) publishFail.get(publishUrl);
                TerminalStatusNotify newTerminalStatusNotify = (TerminalStatusNotify)publishMsg;
                terminalStatusNotify.addMtStatus(newTerminalStatusNotify.getMtStatusNotify().get(0));
            }
        }

        if (!publishResponse.getStatusCode().is2xxSuccessful()){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"publishMessage failed! , type : "+type.getName()+", publishUrl : "+publishUrl);
            System.out.println("publishMessage failed! , type : "+type.getName()+", publishUrl : "+publishUrl);
        } else if (publishResponse.getBody().getCode() != 0){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"publishMessage failed! , type : "+type.getName()+", publishUrl : "+publishUrl+", errmsg : "+publishResponse.getBody().getMessage());
            System.out.println("publishMessage failed! , type : "+type.getName()+", publishUrl : "+publishUrl+", errmsg : "+publishResponse.getBody().getMessage());
        }
    }

    private void serviceRestartPublishMessage(SubscribeMsgTypeEnum type, String publishUrl, Object publishMsg) {
        if (null == publishUrl) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "publishMessage, publishUrl is null ***** ");
            System.out.println("publishMessage, publishUrl is null ***** ");
            return;
        }

        ResponseEntity<BaseResponseMsg>  publishResponse = restClientService.exchangeJson(publishUrl, HttpMethod.POST, publishMsg, null, BaseResponseMsg.class);

        if ( null == publishResponse ){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"Release status failed , publishUrl is error");
            System.out.println("Release status failed , publishUrl is error");
            return;
        }
        if (publishResponse.getStatusCode().is2xxSuccessful() && publishResponse.getBody().getCode() == 0) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "serviceRestartPublishMessage OK! type:" + type.getName() + ", publishUrl:" + publishUrl);
            System.out.println("serviceRestartPublishMessage OK! type:" + type.getName() + ", publishUrl:" + publishUrl);
            /*TerminalStatusNotify terminalStatusNotify = (TerminalStatusNotify) publishMsg;
            System.out.println("newTerminalStatusNotify.getMtStatusNotify().get(0).getDeviceId() : " + terminalStatusNotify.getMtStatusNotify().get(0).getDeviceId());
            terminalMediaSourceService.deleteMtPublish(terminalStatusNotify.getMtStatusNotify().get(0).getDeviceId());*/
            return;
        }

        if (!publishFail.containsKey(publishUrl)) {
            publishFail.put(publishUrl, publishMsg);
        } else {
            if (type == SubscribeMsgTypeEnum.TERMINAL_STATUS) {
                TerminalStatusNotify terminalStatusNotify = (TerminalStatusNotify) publishFail.get(publishUrl);
                TerminalStatusNotify newTerminalStatusNotify = (TerminalStatusNotify) publishMsg;
                terminalStatusNotify.addMtStatus(newTerminalStatusNotify.getMtStatusNotify().get(0));
            }
        }

        if (!publishResponse.getStatusCode().is2xxSuccessful()) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "publishMessage failed! , type:" + type.getName() + ", publishUrl : " + publishUrl);
            System.out.println("publishMessage failed! , type:" + type.getName() + ", publishUrl : " + publishUrl);
        } else if (publishResponse.getBody().getCode() != 0) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "publishMessage failed! , type:" + type.getName() + ", publishUrl:" + publishUrl + ", errmsg:" + publishResponse.getBody().getMessage());
            System.out.println("publishMessage failed! , type:" + type.getName() + ", publishUrl:" + publishUrl + ", errmsg:" + publishResponse.getBody().getMessage());
        }
    }

    @Scheduled(initialDelay = 60 * 1000L, fixedDelay = 3 * 1000L)
    public void checkPublishFail() {
        if (publishFail.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<String, Object>> iterator = publishFail.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> publishMsg = iterator.next();
            ResponseEntity<BaseResponseMsg> publishResponse = restClientService.exchangeJson(publishMsg.getKey(), HttpMethod.POST, publishMsg.getValue(), null, BaseResponseMsg.class);
            if (!publishResponse.getStatusCode().is2xxSuccessful()) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "publishMessage failed! publishUrl : " + publishMsg.getKey());
                System.out.println("publishMessage failed! publishUrl : " + publishMsg.getKey());
                continue;
            }

            if (publishResponse.getBody().getCode() != 0) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "publishMessage failed! publishUrl:" + publishMsg.getKey() + ", errmsg:" + publishResponse.getBody().getMessage());
                System.out.println("publishMessage failed! publishUrl:" + publishMsg.getKey() + ", errmsg:" + publishResponse.getBody().getMessage());
                continue;
            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "publishMessage OK! publishUrl:" + publishMsg.getKey());
            System.out.println("publishMessage OK! publishUrl:" + publishMsg.getKey());
            iterator.remove();
        }
    }

    private static Map<Integer, Map<String, String>> subscribeMsgs = new ConcurrentHashMap<>();
    private Map<String, Object> publishFail = new ConcurrentHashMap<>();


    @Autowired
    private RestClientService restClientService;

    @Autowired
    TerminalMediaSourceService terminalMediaSourceService;

    @Autowired(required = false)
    private McuRestClientService mcuRestClientService;
}
