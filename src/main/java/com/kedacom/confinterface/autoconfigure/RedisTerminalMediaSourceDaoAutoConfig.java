package com.kedacom.confinterface.autoconfigure;

import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import com.kedacom.confinterface.dao.RedisTerminalMediaSourceDao;
import com.kedacom.confinterface.dao.TerminalMediaSourceDao;
import com.kedacom.confinterface.redis.RedisClient;
import com.kedacom.confinterface.redis.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableConfigurationProperties(RedisConfig.class)
@ConditionalOnProperty(name = "confinterface.sys.memDBType", havingValue = "redis", matchIfMissing = false)
public class RedisTerminalMediaSourceDaoAutoConfig {

    @Autowired
    private RedisConfig redisConfig;

    private JedisPoolConfig jedisPoolConfig() {

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        jedisPoolConfig.setMaxIdle(redisConfig.getMaxIdle());
        jedisPoolConfig.setMaxTotal(redisConfig.getMaxTotal());
        jedisPoolConfig.setMaxWaitMillis(redisConfig.getMaxWaitMillis());
        // 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        jedisPoolConfig.setMinEvictableIdleTimeMillis(redisConfig.getMinEvictableIdleTimeMillis());
        // 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        jedisPoolConfig.setNumTestsPerEvictionRun(redisConfig.getNumTestsPerEvictionRun());
        // 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(redisConfig.getTimeBetweenEvictionRunsMillis());
        jedisPoolConfig.setTestOnBorrow(redisConfig.isTestOnBorrow());
        jedisPoolConfig.setTestWhileIdle(redisConfig.isTestWhileIdle());


        return jedisPoolConfig;
    }

    private RedisClusterConfiguration redisClusterConfiguration() {
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        String[] redisClusterAddrs = redisConfig.getClusterNodes().split(",");

        Set<RedisNode> redisNodeSet = new HashSet<>();

        //ipv4：ip:port
        //ipv6: [ip]:port
        for (String ipPort : redisClusterAddrs) {
            if (!ipPort.contains("]")) {
                //ipv4地址
                String[] ipAndPort = ipPort.split(":");
                redisNodeSet.add(new RedisNode(ipAndPort[0].trim(), Integer.valueOf(ipAndPort[1])));
            } else {
                //ipv6地址
                String[] ipAndPort = ipPort.split("]:");
                redisNodeSet.add(new RedisNode(ipAndPort[0].substring(1), Integer.valueOf(ipAndPort[1])));
            }
        }

        redisClusterConfiguration.setClusterNodes(redisNodeSet);
        redisClusterConfiguration.setMaxRedirects(redisConfig.getMaxRedirects());

        String password = redisConfig.getPassword();
        if (null != password && !password.isEmpty()) {
            redisClusterConfiguration.setPassword(RedisPassword.of(password));
        }

        return redisClusterConfiguration;
    }

    @Bean
    @ConditionalOnProperty(name = "confinterface.redis.mode", havingValue = "single", matchIfMissing = true)
    public JedisConnectionFactory standaloneConnectionFactory(){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"create standalone connection factory, hostName:"+redisConfig.getHostName());
        System.out.println("create standalone connection factory, hostName:"+redisConfig.getHostName());

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisConfig.getHostName(), redisConfig.getPort());

        redisStandaloneConfiguration.setDatabase(redisConfig.getSelectDatabase());
        String password = redisConfig.getPassword();
        if (null != password && !password.isEmpty()) {
            redisStandaloneConfiguration.setPassword(RedisPassword.of(password));
        }

        JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder().usePooling().poolConfig(jedisPoolConfig()).build();
        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
    }

    @Bean
    @ConditionalOnProperty(name = "confinterface.redis.mode", havingValue = "cluster", matchIfMissing = false)
    public JedisConnectionFactory clusterConnectionFactory(){
        LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"create cluster connection factory!!!!");
        System.out.println("create cluster connection factory!!!!");

        JedisPoolConfig jedisPoolConfig = jedisPoolConfig();
        RedisClusterConfiguration redisClusterConfig = redisClusterConfiguration();

        return new JedisConnectionFactory(redisClusterConfig, jedisPoolConfig);
    }

    private void initRedisTemplate(RedisTemplate<String, Object> redisTemplate, RedisConnectionFactory factory) {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.setConnectionFactory(factory);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        initRedisTemplate(redisTemplate, redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public RedisClient redisClient(RedisTemplate<String, Object> redisTemplate) {
        RedisClient redisClient = new RedisClient();
        redisClient.setRedisTemplate(redisTemplate);
        return redisClient;
    }

    @Bean
    public TerminalMediaSourceDao terminalMediaSourceDao(RedisClient redisClient) {
        return new RedisTerminalMediaSourceDao(redisClient);
    }
}
