package com.kedacom.confinterface.acutor;

import com.kedacom.confinterface.dao.ComponentStatusErrorEnum;
import com.kedacom.confinterface.h323.H323ProtocalConfig;

import com.kedacom.confinterface.service.TerminalManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "confinterface.h323.useGK", havingValue = "true", matchIfMissing = false)
public class GkHealthIndicator extends AbstractHealthIndicator {

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        // Use the builder to build the health status details that should be reported.
        // If you throw an exception, the status will be DOWN with the exception message.
        if(terminalManageService.getUseGkStatus().get() == 0){
            status = Status.DOWN;
        }else{
            status = Status.UP;
        }

        builder.status(status)
                .withDetail("address", h323ProtocalConfig.getGkIp()+":"+h323ProtocalConfig.getGkCallPort());
        if (Status.DOWN == status){
            builder.withDetail("error", ComponentStatusErrorEnum.GKREGISTERFAILED.getMessage());
        }
    }


    @Autowired(required = false)
    private H323ProtocalConfig h323ProtocalConfig;

    private Status status = Status.UNKNOWN;

    @Autowired(required = false)
    private TerminalManageService terminalManageService;
}