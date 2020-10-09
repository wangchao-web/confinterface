package com.kedacom.confinterface.autoconfigure;

import com.kedacom.confinterface.h323.H323TerminalManageService;
import com.kedacom.confinterface.h323plus.H323PlusProtocalConfig;
import com.kedacom.confinterface.h323plus.H323PlusTerminalManageService;
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
@EnableConfigurationProperties(H323PlusProtocalConfig.class)
@ConditionalOnClass(H323TerminalManageService.class)
@ConditionalOnProperty(name = "confinterface.sys.protocalType", havingValue = "h323Plus", matchIfMissing = false)
@EnableAsync(proxyTargetClass=true)
public class H323PlusTerminalManageServiceAutoConfig {
    @Autowired
    private H323PlusProtocalConfig h323PlusProtocalConfig;

    @Bean
    @ConditionalOnMissingBean(H323TerminalManageService.class)
    public TerminalManageService sipTerminalManageService(TerminalMediaSourceService terminalMediaSourceService) {
        return new H323PlusTerminalManageService(h323PlusProtocalConfig, terminalMediaSourceService);
    }
}
