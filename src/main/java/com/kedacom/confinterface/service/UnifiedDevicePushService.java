package com.kedacom.confinterface.service;

import com.kedacom.confinterface.dto.BaseResponseMsg;
import com.kedacom.confinterface.dto.UnifiedDevicePushTerminalStatus;
import com.kedacom.confinterface.restclient.RestClientService;
import com.kedacom.confinterface.syssetting.BaseSysConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;

@Service
@EnableScheduling
public class UnifiedDevicePushService {
    @Async("confTaskExecutor")
    public void publishMtStatus(UnifiedDevicePushTerminalStatus unifiedDevicePushTerminalStatus){
        if (statusUrl.length() == 0) {
            statusUrl.append("http://");
            statusUrl.append(baseSysConfig.getCdeviceManageSrvAddr());
            statusUrl.append("/cdevice-manage/devices/callCode/status");
        }

        ResponseEntity<BaseResponseMsg> publishResponse = restClientService.exchangeJson(statusUrl.toString(), HttpMethod.PUT, unifiedDevicePushTerminalStatus, null, BaseResponseMsg.class);
        if (publishResponse.getStatusCode().is2xxSuccessful() && publishResponse.getBody().getCode() == 0) {
            System.out.println("publishMtStatus OK! callCode:"+unifiedDevicePushTerminalStatus.getCallCode());
            return;
        }

        publishFail.add(unifiedDevicePushTerminalStatus);

        if (!publishResponse.getStatusCode().is2xxSuccessful()){
            System.out.println("publishMtStatus failed! , statusCode : "+publishResponse.getStatusCode());
        } else if (publishResponse.getBody().getCode() != 0){
            System.out.println("publishMtStatus failed! , errCode:"+publishResponse.getBody().getCode()+", errmsg:"+publishResponse.getBody().getMessage());
        }
    }

    @Scheduled(initialDelay = 60*1000L, fixedDelay = 3 * 1000L)
    public void checkPublishFail(){
        if (publishFail.isEmpty()) {
            return;
        }

        Iterator<UnifiedDevicePushTerminalStatus> iterator = publishFail.iterator();
        while (iterator.hasNext()){
            UnifiedDevicePushTerminalStatus unifiedDevicePushTerminalStatus = iterator.next();
            ResponseEntity<BaseResponseMsg> publishResponse = restClientService.exchangeJson(statusUrl.toString(), HttpMethod.PUT, unifiedDevicePushTerminalStatus, null, BaseResponseMsg.class);
            if (!publishResponse.getStatusCode().is2xxSuccessful()){
                System.out.println("checkPublishFail, publishMtStatus failed! callCode:"+unifiedDevicePushTerminalStatus.getCallCode()+", statusCode:"+publishResponse.getStatusCode());
                continue;
            }

            if (publishResponse.getBody().getCode() != 0){
                System.out.println("checkPublishFail, publishMtStatus failed! errmsg:"+publishResponse.getBody().getMessage());
                continue;
            }

            System.out.println("checkPublishFail, publishMtStatus OK! callCode:"+unifiedDevicePushTerminalStatus.getCallCode());
            iterator.remove();
        }
    }

    private LinkedBlockingDeque<UnifiedDevicePushTerminalStatus> publishFail = new LinkedBlockingDeque<>();

    @Autowired
    private RestClientService restClientService;

    @Autowired
    private BaseSysConfig baseSysConfig;

    private StringBuffer statusUrl = new StringBuffer();
}


