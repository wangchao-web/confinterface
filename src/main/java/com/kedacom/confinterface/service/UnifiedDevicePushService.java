package com.kedacom.confinterface.service;

import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dto.BaseResponseMsg;
import com.kedacom.confinterface.dto.MediaResource;
import com.kedacom.confinterface.dto.UnifiedDevicePushTerminalStatus;
import com.kedacom.confinterface.inner.SubscribeMsgTypeEnum;
import com.kedacom.confinterface.inner.TerminalOnlineStatusEnum;
import com.kedacom.confinterface.restclient.RestClientService;
import com.kedacom.confinterface.syssetting.BaseSysConfig;
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
import java.util.concurrent.LinkedBlockingDeque;

@ConditionalOnProperty(name = "confinterface.sys.pushServiceType", havingValue = "ctsp")
@Service
@EnableScheduling
public class UnifiedDevicePushService extends ConfInterfacePublishService{

    @Override
    public void publishStatus(String account, String groupId, int status, List<MediaResource> forwardResources, List<MediaResource> reverseResources,String callMode) {
        publishStatus(account, groupId, status);
    }

    @Override
    @Async("confTaskExecutor")
    public void publishStatus(String account, String groupId, int status) {
        if (status == TerminalOnlineStatusEnum.DUALSTREAM.getCode())
            return;

        UnifiedDevicePushTerminalStatus unifiedDevicePushTerminalStatus = new UnifiedDevicePushTerminalStatus(account, groupId, status);
        publishMtStatus(unifiedDevicePushTerminalStatus);
    }

    @Override
    public void publishStatus(String account, String groupId, int status, int faileCode) {
            return;
    }

    @Override
    public void publishStatus(SubscribeMsgTypeEnum type, String publishUrl, Object publishMsg) {
        return;
    }

    @Override
    public void cancelSubscribeMessage(int type, String groupId, String url) {

    }

    @Override
    public void addSubscribeMessage(int type, String groupId, String url) {

    }

    private void publishMtStatus(UnifiedDevicePushTerminalStatus unifiedDevicePushTerminalStatus){
        if (statusUrl.length() == 0) {
            statusUrl.append("http://");
            statusUrl.append(baseSysConfig.getCdeviceManageSrvAddr());
            statusUrl.append("/cdevice-manage/devices/callCode/status");
        }

        ResponseEntity<BaseResponseMsg> publishResponse = restClientService.exchangeJson(statusUrl.toString(), HttpMethod.PUT, unifiedDevicePushTerminalStatus, null, BaseResponseMsg.class);
        if (publishResponse.getStatusCode().is2xxSuccessful() && publishResponse.getBody().getCode() == 0) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"publishMtStatus OK! callCode:"+unifiedDevicePushTerminalStatus.getCallCode());
            System.out.println("publishMtStatus OK! callCode:"+unifiedDevicePushTerminalStatus.getCallCode());
            return;
        }

        publishFail.add(unifiedDevicePushTerminalStatus);

        if (!publishResponse.getStatusCode().is2xxSuccessful()){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"publishMtStatus failed! , statusCode : "+publishResponse.getStatusCode());
            System.out.println("publishMtStatus failed! , statusCode : "+publishResponse.getStatusCode());
        } else if (publishResponse.getBody().getCode() != 0){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"publishMtStatus failed! , errCode:"+publishResponse.getBody().getCode()+", errmsg:"+publishResponse.getBody().getMessage());
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
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"checkPublishFail, publishMtStatus failed! callCode:"+unifiedDevicePushTerminalStatus.getCallCode()+", statusCode:"+publishResponse.getStatusCode());
                System.out.println("checkPublishFail, publishMtStatus failed! callCode:"+unifiedDevicePushTerminalStatus.getCallCode()+", statusCode:"+publishResponse.getStatusCode());
                continue;
            }

            if (publishResponse.getBody().getCode() != 0){
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"checkPublishFail, publishMtStatus failed! errmsg:"+publishResponse.getBody().getMessage());
                System.out.println("checkPublishFail, publishMtStatus failed! errmsg:"+publishResponse.getBody().getMessage());
                continue;
            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"checkPublishFail, publishMtStatus OK! callCode:"+unifiedDevicePushTerminalStatus.getCallCode());
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


