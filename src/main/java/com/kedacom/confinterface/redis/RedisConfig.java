package com.kedacom.confinterface.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "confinterface.redis")
public class RedisConfig {

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public void setMaxWaitMillis(int maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    public int getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public String getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(String clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxRedirects() {
        return maxRedirects;
    }

    public void setMaxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("maxIdle:").append(maxIdle)
                .append(",maxTotal:").append(maxTotal)
                .append(",maxWaitMillis:").append(maxWaitMillis)
                .append(",minEvictableIdleTimeMillis:").append(minEvictableIdleTimeMillis)
                .append(",numTestsPerEvictionRun:").append(numTestsPerEvictionRun)
                .append(",timeBetweenEvictionRunsMillis:").append(timeBetweenEvictionRunsMillis)
                .append(",testOnBorrow:").append(testOnBorrow)
                .append(",testWhileIdle:").append(testWhileIdle)
                .append(", mode:").append(mode)
                .append(", clusterNode:").append(clusterNodes)
                .append(",hostName:").append(hostName)
                .append(",port:").append(port)
                .append(",maxRedirects:").append(maxRedirects)
                .toString();
    }

    //连接池中最多有多少空闲的连接数量
    private int maxIdle = 50;
    //连接池中最大的数据库连接数量
    private int maxTotal = 300;
    //连接建立的最大等待时间，单位毫秒
    private int maxWaitMillis = 3000;
    //逐出连接的最小空闲时间，单位毫秒
    private int minEvictableIdleTimeMillis = 60000;
    //每次逐出检查时，逐出的最大连接数目
    private int numTestsPerEvictionRun = 3;
    //逐出扫描的时间间隔，负数不执行逐出扫描
    private long timeBetweenEvictionRunsMillis = -1;
    //从连接池中取出连接前是否检查，如果检验失败,则从连接池中去除连接并尝试取出另一个
    private boolean testOnBorrow = true;
    //是否在空闲时候检查有效性
    private boolean testWhileIdle = true;
    private String mode = "single";   //single or cluster
    private String clusterNodes;
    private String hostName="127.0.0.1";
    private String password;
    private int port=6379;
    //在执行失败后，进行的重试次数
    private int maxRedirects = 3;
}
