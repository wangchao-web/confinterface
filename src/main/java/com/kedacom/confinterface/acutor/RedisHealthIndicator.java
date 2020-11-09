package com.kedacom.confinterface.acutor;

import com.kedacom.confinterface.redis.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.redis.connection.ClusterInfo;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Properties;

@Component
public class RedisHealthIndicator extends AbstractHealthIndicator {

    static final String VERSION = "version";

    static final String REDIS_VERSION = "redis_version";

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisHealthIndicator(RedisConnectionFactory connectionFactory) {
        super("Redis health check failed");
        Assert.notNull(connectionFactory, "ConnectionFactory must not be null");
        this.redisConnectionFactory = connectionFactory;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        RedisConnection connection = null;
        try {
            connection = RedisConnectionUtils
                    .getConnection(this.redisConnectionFactory);

            if (connection instanceof RedisClusterConnection) {
                ClusterInfo clusterInfo = ((RedisClusterConnection) connection)
                        .clusterGetClusterInfo();
                builder.up().withDetail("cluster_size", clusterInfo.getClusterSize())
                        .withDetail("slots_up", clusterInfo.getSlotsOk())
                        .withDetail("slots_fail", clusterInfo.getSlotsFail());
            } else {
                Properties info = connection.info();
                builder.up().withDetail(VERSION, info.getProperty(REDIS_VERSION))
                        .withDetail("address", redisConfig.getHostName() + ":" + redisConfig.getPort())
                        .withDetail("encrypt", 0)
                        .withDetail("password", "kdypos" + redisConfig.getPassword());
            }
        } catch (Exception e) {
            builder.down()
                    .withDetail("address", redisConfig.getHostName() + ":" + redisConfig.getPort())
                    .withDetail("encrypt", 0)
                    .withDetail("password", "kdypos" + redisConfig.getPassword())
                    .withDetail("error", e.getMessage());
        } finally {
            RedisConnectionUtils.releaseConnection(connection,
                    this.redisConnectionFactory);
        }
    }

    @Autowired
    private RedisConfig redisConfig;
}