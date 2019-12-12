package com.kedacom.confinterface.service;

import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dto.BaseResponseMsg;
import com.kedacom.confinterface.dto.MediaResource;
import com.kedacom.confinterface.dto.TerminalStatus;
import com.kedacom.confinterface.dto.TerminalStatusNotify;
import com.kedacom.confinterface.inner.SubscribeMsgTypeEnum;
import com.kedacom.confinterface.inner.TerminalOnlineStatusEnum;
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
public class  SchedulePublishService extends ConfInterfacePublishService{

    @Override
    public void publishStatus(String account, String groupId, int status) {
        publishStatus(account, groupId, status, null, null);
    }

    @Override
    public void publishStatus(String account, String groupId, int status, List<MediaResource> forwardResources, List<MediaResource> reverseResources) {
        System.out.println("now in confInterfacePublishService publishStatus!!");
        String accountType = "MT";

        if (account.equals(groupId)) {
            System.out.println("publishStatus, type: Conference!");
            account = "Conference";
        } else if (status == TerminalOnlineStatusEnum.DUALSTREAM.getCode()) {
            System.out.println("publishStatus, type: Dual!");
            accountType = "Dual";
        }

        TerminalStatusNotify terminalStatusNotify = new TerminalStatusNotify();
        TerminalStatus terminalStatus = new TerminalStatus(account, accountType, status, forwardResources, reverseResources);
        terminalStatusNotify.addMtStatus(terminalStatus);
        publishMessage(SubscribeMsgTypeEnum.TERMINAL_STATUS, groupId, terminalStatusNotify);
    }

    @Override
    @Async("confTaskExecutor")
    public void addSubscribeMessage(int type, String groupId, String url){
        synchronized (this) {
            Map<String, String> groupUrl = subscribeMsgs.get(type);
            if (null == groupUrl) {
                groupUrl = new ConcurrentHashMap<>();
                subscribeMsgs.put(type, groupUrl);
            }

            groupUrl.put(groupId, url);
        }
    }

    private void publishMessage(SubscribeMsgTypeEnum type, String groupId, Object publishMsg){
        Map<String, String> groupUrls = subscribeMsgs.get(type.getType());
        System.out.println("restClientService : "+restClientService);
        if (null == groupUrls) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"publishMessage, has no client subscribe message, type :"+type.getType()+", name:"+type.getName());
            System.out.println("publishMessage, has no client subscribe message, type :"+type.getType()+", name:"+type.getName());
            return;
        }

        String publishUrl = groupUrls.get(groupId);
        if (null == publishUrl){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"publishMessage, has no client subscribe message("+type.getName()+") of group("+groupId+")");
            System.out.println("publishMessage, has no client subscribe message("+type.getName()+") of group("+groupId+")");
            return;
        }

        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"publishMessage, groupId:" + groupId);
        System.out.println("publishMessage, groupId:"+groupId);
        ResponseEntity<BaseResponseMsg> publishResponse = restClientService.exchangeJson(publishUrl, HttpMethod.POST, publishMsg, null, BaseResponseMsg.class);
        if (publishResponse.getStatusCode().is2xxSuccessful() && publishResponse.getBody().getCode() == 0) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"publishMessage OK! type:"+type.getName()+", publishUrl:"+publishUrl);
            System.out.println("publishMessage OK! type:"+type.getName()+", publishUrl:"+publishUrl);
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
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"publishMessage failed! , type:"+type.getName()+", publishUrl : "+publishUrl);
            System.out.println("publishMessage failed! , type:"+type.getName()+", publishUrl : "+publishUrl);
        } else if (publishResponse.getBody().getCode() != 0){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"publishMessage failed! , type:"+type.getName()+", publishUrl:"+publishUrl+", errmsg:"+publishResponse.getBody().getMessage());
            System.out.println("publishMessage failed! , type:"+type.getName()+", publishUrl:"+publishUrl+", errmsg:"+publishResponse.getBody().getMessage());
        }
    }

    @Scheduled(initialDelay = 60*1000L, fixedDelay = 3 * 1000L)
    public void checkPublishFail(){
        if (publishFail.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<String, Object>> iterator = publishFail.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Object> publishMsg = iterator.next();
            ResponseEntity<BaseResponseMsg> publishResponse = restClientService.exchangeJson(publishMsg.getKey(), HttpMethod.POST, publishMsg.getValue(), null, BaseResponseMsg.class);
            if (!publishResponse.getStatusCode().is2xxSuccessful()){
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"publishMessage failed! publishUrl : "+publishMsg.getKey());
                System.out.println("publishMessage failed! publishUrl : "+publishMsg.getKey());
                continue;
            }

            if (publishResponse.getBody().getCode() != 0){
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"publishMessage failed! publishUrl:"+publishMsg.getKey()+", errmsg:"+publishResponse.getBody().getMessage());
                System.out.println("publishMessage failed! publishUrl:"+publishMsg.getKey()+", errmsg:"+publishResponse.getBody().getMessage());
                continue;
            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"publishMessage OK! publishUrl:"+publishMsg.getKey());
            System.out.println("publishMessage OK! publishUrl:"+publishMsg.getKey());
            iterator.remove();
        }
    }

    private static Map<Integer, Map<String, String>> subscribeMsgs = new ConcurrentHashMap<>();
    private  Map<String, Object> publishFail = new ConcurrentHashMap<>();


    @Autowired
    private RestClientService restClientService;
}
