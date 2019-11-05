package com.kedacom.confinterface.listenser;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "confinterface.sys.useMcu", havingValue = "true", matchIfMissing = true)
@Configuration
public class ListenserConfig {

    @Bean
    public SubscribeEventListenser subscribeEventListenser(){
        return new SubscribeEventListenser();
    }
}