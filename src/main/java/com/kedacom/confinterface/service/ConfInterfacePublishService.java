package com.kedacom.confinterface.service;

import com.kedacom.confinterface.dto.BaseResponseMsg;
import com.kedacom.confinterface.dto.TerminalStatusNotify;
import com.kedacom.confinterface.inner.SubscribeMsgTypeEnum;
import com.kedacom.confinterface.restclient.RestClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@EnableScheduling
public class ConfInterfacePublishService {

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

    public void publishMessage(SubscribeMsgTypeEnum type, String groupId, Object publishMsg){
        Map<String, String> groupUrls = subscribeMsgs.get(type.getType());
        if (null == groupUrls) {
            System.out.println("publishMessage, has no client subscribe message, type :"+type.getType()+", name:"+type.getName());
            return;
        }

        String publishUrl = groupUrls.get(groupId);
        if (null == publishUrl){
            System.out.println("publishMessage, has no client subscribe message("+type.getName()+") of group("+groupId+")");
            return;
        }

        System.out.println("publishMessage, groupId:"+groupId);
        ResponseEntity<BaseResponseMsg> publishResponse = restClientService.exchangeJson(publishUrl, HttpMethod.POST, publishMsg, null, BaseResponseMsg.class);
        if (publishResponse.getStatusCode().is2xxSuccessful() && publishResponse.getBody().getCode() == 0) {
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
            System.out.println("publishMessage failed! , type:"+type.getName()+", publishUrl : "+publishUrl);
        } else if (publishResponse.getBody().getCode() != 0){
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
                System.out.println("publishMessage failed! publishUrl : "+publishMsg.getKey());
                continue;
            }

            if (publishResponse.getBody().getCode() != 0){
                System.out.println("publishMessage failed! publishUrl:"+publishMsg.getKey()+", errmsg:"+publishResponse.getBody().getMessage());
                continue;
            }

            System.out.println("publishMessage OK! publishUrl:"+publishMsg.getKey());
            iterator.remove();
        }
    }

    private Map<Integer, Map<String, String>> subscribeMsgs = new ConcurrentHashMap<>();
    private Map<String, Object> publishFail = new ConcurrentHashMap<>();

    @Autowired
    private RestClientService restClientService;
}
