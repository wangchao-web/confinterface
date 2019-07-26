package com.kedacom.confinterface.restclient;

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
            System.out.println("loginSuccess : " +loginSuccess);
            return loginSuccess;

        } catch (Exception e) {
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
