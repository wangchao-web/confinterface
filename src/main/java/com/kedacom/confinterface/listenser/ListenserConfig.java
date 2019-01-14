package com.kedacom.confinterface.listenser;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ListenserConfig {

    @Bean
    public SubscribeEventListenser subscribeEventListenser(){
        return new SubscribeEventListenser();
    }
}