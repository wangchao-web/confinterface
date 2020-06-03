package com.kedacom.confinterface.autoconfigure;

import com.kedacom.confinterface.h323.H323ProtocalConfig;
import com.kedacom.confinterface.h323.H323TerminalManageService;
import com.kedacom.confinterface.service.TerminalManageService;
import com.kedacom.confinterface.service.TerminalMediaSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableConfigurationProperties(H323ProtocalConfig.class)
@ConditionalOnClass(H323TerminalManageService.class)
@ConditionalOnProperty(name = "confinterface.sys.protocalType", havingValue = "h323", matchIfMissing = false)
@EnableAsync(proxyTargetClass=true)
public class H323TerminalManageServiceAutoConfig {

    @Autowired
    private H323ProtocalConfig h323ProtocalConfig;

    @Bean
    @ConditionalOnMissingBean(H323TerminalManageService.class)
    public TerminalManageService h323TerminalManageService(TerminalMediaSourceService terminalMediaSourceService) {
        return new H323TerminalManageService(h323ProtocalConfig, terminalMediaSourceService);
    }
}
