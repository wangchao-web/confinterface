package com.kedacom.confinterface.acutor;

import com.kedacom.confinterface.restclient.McuRestClientService;
import com.kedacom.confinterface.restclient.McuRestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "confinterface.sys.useMcu", havingValue = "true", matchIfMissing = false)
public class McuHealthIndicator extends AbstractHealthIndicator {

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        // Use the builder to build the health status details that should be reported.
        // If you throw an exception, the status will be DOWN with the exception message.
        if (mcuRestClientService.loginSuccess) {
            status = Status.UP;
        } else {
            status = Status.DOWN;
        }
        if(mcuRestConfig.getMcuRestPort()>0){
            address = mcuRestConfig.getMcuIp()+":"+mcuRestConfig.getMcuRestPort();
        }else{
            address = mcuRestConfig.getMcuIp();
        }
        builder.status(status)
                .withDetail("address", address)
                .withDetail("encrypt", 0)
                .withDetail("username", mcuRestConfig.getUsername())
                .withDetail("password", "kdypos"+mcuRestConfig.getPassword());
        if(status == Status.DOWN){
            builder.withDetail("error",mcuRestClientService.mcuStatusError.getMessage());
        }
    }


    @Autowired(required = false)
    private McuRestConfig mcuRestConfig;

    @Autowired(required = false)
    private McuRestClientService mcuRestClientService;

    private Status status = Status.UNKNOWN;
    private String address ;
}

