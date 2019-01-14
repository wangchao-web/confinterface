package com.kedacom.confinterface;

import com.kedacom.confinterface.util.ConfInterfaceThreadPoolConfig;

import org.apache.log4j.PropertyConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
@EnableConfigurationProperties(ConfInterfaceThreadPoolConfig.class)
@EnableCaching
public class ConfinterfaceApplication {

    public static void main(String[] args) {
        PropertyConfigurator.configure(System.getProperty("user.dir")+"/conf/log4j.properties");
        SpringApplication.run(ConfinterfaceApplication.class, args);
    }
}
