package com.kedacom.confinterface.restclient;

import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.syssetting.BaseSysConfig;
import com.kedacom.mcuadapter.IMcuClient;
import com.kedacom.mcuadapter.IMcuClientManager;
import com.kedacom.mcuadapter.McuManagerInitInfo;
import com.kedacom.mcuadapter.common.McuClientLoginInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@EnableConfigurationProperties(BaseSysConfig.class)
public class McuSdkClientService {

    public boolean login() {
        if (null == mcuClientManager)
            return false;

        try {
            if (null == mcuClient) {
                mcuClient = mcuClientManager.createMcuClient();
            }

            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"McuSdkClientService, mcuSdkConfig:" + baseSysConfig);
            System.out.println("McuSdkClientService, mcuSdkConfig:" + baseSysConfig);

            McuClientLoginInfo mcuClientLoginInfo = new McuClientLoginInfo();
            mcuClientLoginInfo.setIp(mcuRestConfig.getMcuIp());
            mcuClientLoginInfo.setUsername(mcuRestConfig.getUsername());
            mcuClientLoginInfo.setPassword(mcuRestConfig.getPassword());

            if (mcuClient.connectToServer(mcuClientLoginInfo)){
                loginSuccess = true;
            }else{
                loginSuccess = false;
            }
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"loginSuccess : " +loginSuccess);
            System.out.println("loginSuccess : " +loginSuccess);
            return loginSuccess;

        } catch (Exception e) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"sdk mcu, login failed!");
            System.out.println("sdk mcu, login failed!");
            e.printStackTrace();
            return false;
        }
    }

    public void setMcuClientManager(IMcuClientManager mcuClientManager) {
        this.mcuClientManager = mcuClientManager;
    }

    public boolean initMcuSdk() {
        McuManagerInitInfo mcuManagerInitInfo = new McuManagerInitInfo();
        mcuManagerInitInfo.setLogPath(baseSysConfig.getLogPath());
        mcuManagerInitInfo.setLogFileSize(baseSysConfig.getLogFileSize());
        mcuManagerInitInfo.setLogFileNum(baseSysConfig.getLogFileNum());

        int tryTimes = 3;
        while (tryTimes-- > 0) {
            if (mcuClientManager.init(mcuManagerInitInfo))
                return true;

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"initMcuSdk ..... currentTime : " + System.currentTimeMillis());
                System.out.println("initMcuSdk ..... currentTime : " + System.currentTimeMillis());
            }
        }

        return false;
    }

    @Autowired
    private BaseSysConfig baseSysConfig;

    private IMcuClient mcuClient;

    @Autowired
    private McuRestConfig mcuRestConfig;

    private IMcuClientManager mcuClientManager;

    protected volatile boolean loginSuccess;

}
