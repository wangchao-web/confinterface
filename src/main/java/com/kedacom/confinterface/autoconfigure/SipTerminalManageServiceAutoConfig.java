package com.kedacom.confinterface.autoconfigure;

import com.kedacom.confinterface.service.TerminalMediaSourceService;
import com.kedacom.confinterface.sip.SipTerminalManageService;
import com.kedacom.confinterface.service.TerminalManageService;
import com.kedacom.confinterface.sip.SipProtocalConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableConfigurationProperties(SipProtocalConfig.class)
@ConditionalOnClass(SipTerminalManageService.class)
@ConditionalOnProperty(name = "confinterface.sys.protocalType", havingValue = "sip", matchIfMissing = false)
@EnableAsync(proxyTargetClass=true)
public class SipTerminalManageServiceAutoConfig {

    @Autowired
    private SipProtocalConfig sipProtocalConfig;

    @Bean
    @ConditionalOnMissingBean(SipTerminalManageService.class)
    public TerminalManageService sipTerminalManageService(TerminalMediaSourceService terminalMediaSourceService) {
        return new SipTerminalManageService(sipProtocalConfig, terminalMediaSourceService);
    }
}
