package com.kedacom.confinterface.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ConfInterfaceTaskExecutor {

    @Autowired
    private ConfInterfaceThreadPoolConfig threadPoolConfig;

    @Bean
    public AsyncTaskExecutor confTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(threadPoolConfig.getCorePoolSize());
        taskExecutor.setMaxPoolSize(threadPoolConfig.getMaxPoolSize());
        taskExecutor.setQueueCapacity(threadPoolConfig.getQueueCapacity());
        taskExecutor.setKeepAliveSeconds(threadPoolConfig.getKeepAliveSeconds());
        taskExecutor.setThreadNamePrefix("ConfExecutor-");

        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        return taskExecutor;
    }
}
