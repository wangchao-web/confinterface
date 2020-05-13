package com.kedacom.confinterface;

import com.kedacom.confinterface.LogService.LogHandler;
import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.util.ConfInterfaceThreadPoolConfig;

import org.apache.log4j.PropertyConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
@EnableConfigurationProperties(ConfInterfaceThreadPoolConfig.class)
@EnableCaching
public class ConfinterfaceApplication {
    //处理读取不到配置文件时,结束服务进程
    static {
        int num = 0;
        int maxNum = 3;
        while (true) {
            try {
                new FileInputStream("application.properties");
            } catch (FileNotFoundException e) {
                LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Config file not found exception!");
                System.out.println("Config file not found exception!");
                e.printStackTrace();
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                num++;
                if (num < maxNum) {
                    continue;
                }
            }
            break;
        }
        if (num < maxNum) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Check config file succeed!");
            System.out.println("Check config file succeed!");
        } else {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Check config file failed!");
            System.exit(0);
        }
    }

    //处理读取不到配置文件时,结束服务进程
    static {
        int num = 0;
        int maxNum = 3;
        while (true) {
            try {
                new FileInputStream("application.properties");
            } catch (FileNotFoundException e) {
                LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Config file not found exception!");
                System.out.println("Config file not found exception!");
                e.printStackTrace();
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                num++;
                if (num < maxNum) {
                    continue;
                }
            }
            break;
        }
        if (num < maxNum) {
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Check config file succeed!");
            System.out.println("Check config file succeed!");
        } else {
            LogTools.error(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE, "Check config file failed!");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        PropertyConfigurator.configure(System.getProperty("user.dir") + "/conf/log4j.properties");
        ExecutorService logQueueHandler = Executors.newFixedThreadPool(1);
        logQueueHandler.execute(new LogHandler());
        SpringApplication.run(ConfinterfaceApplication.class, args);
    }
}
