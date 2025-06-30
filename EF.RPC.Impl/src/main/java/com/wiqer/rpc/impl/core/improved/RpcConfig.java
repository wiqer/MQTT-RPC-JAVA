package com.wiqer.rpc.impl.core.improved;

import java.util.concurrent.TimeUnit;

/**
 * RPC配置类
 * 改进设计：统一的配置管理，支持配置验证
 */
public class RpcConfig {
    
    // 连接配置
    private int maxConnections = 100;
    private int minConnections = 10;
    private long connectionTimeout = 30000; // 30秒
    private long maxIdleTime = 300000; // 5分钟
    
    // 超时配置
    private long requestTimeout = 30000; // 30秒
    private long heartbeatInterval = 30000; // 30秒
    
    // 序列化配置
    private String serializerType = "json"; // json, protobuf
    private boolean enableCompression = false;
    
    // 重试配置
    private int maxRetries = 3;
    private long retryDelay = 1000; // 1秒
    
    // 负载均衡配置
    private String loadBalanceStrategy = "round_robin"; // round_robin, random, least_connections
    
    // 监控配置
    private boolean enableMetrics = true;
    private boolean enableHealthCheck = true;
    private long metricsInterval = 60000; // 1分钟
    
    // 日志配置
    private String logLevel = "INFO";
    private boolean enableRequestLog = true;
    private boolean enableResponseLog = false;
    
    // 安全配置
    private boolean enableSsl = false;
    private String sslCertPath;
    private String sslKeyPath;
    
    // 线程池配置
    private int coreThreads = 16;
    private int maxThreads = 32;
    private long threadKeepAliveTime = 60000; // 1分钟
    private int threadQueueSize = 1000;
    
    /**
     * 创建默认配置
     */
    public static RpcConfig createDefault() {
        return new RpcConfig();
    }
    
    /**
     * 创建高性能配置
     */
    public static RpcConfig createHighPerformance() {
        RpcConfig config = new RpcConfig();
        config.setMaxConnections(500);
        config.setCoreThreads(32);
        config.setMaxThreads(64);
        config.setEnableCompression(true);
        config.setSerializerType("protobuf");
        return config;
    }
    
    /**
     * 创建高可用配置
     */
    public static RpcConfig createHighAvailability() {
        RpcConfig config = new RpcConfig();
        config.setMaxRetries(5);
        config.setEnableHealthCheck(true);
        config.setEnableMetrics(true);
        config.setLoadBalanceStrategy("least_connections");
        return config;
    }
    
    /**
     * 验证配置
     */
    public void validate() {
        if (maxConnections < minConnections) {
            throw new IllegalArgumentException("maxConnections must be >= minConnections");
        }
        if (connectionTimeout <= 0) {
            throw new IllegalArgumentException("connectionTimeout must be > 0");
        }
        if (requestTimeout <= 0) {
            throw new IllegalArgumentException("requestTimeout must be > 0");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be >= 0");
        }
        if (coreThreads > maxThreads) {
            throw new IllegalArgumentException("coreThreads must be <= maxThreads");
        }
        if (!serializerType.equals("json") && !serializerType.equals("protobuf")) {
            throw new IllegalArgumentException("serializerType must be 'json' or 'protobuf'");
        }
    }
    
    // Getters and Setters
    public int getMaxConnections() {
        return maxConnections;
    }
    
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    public int getMinConnections() {
        return minConnections;
    }
    
    public void setMinConnections(int minConnections) {
        this.minConnections = minConnections;
    }
    
    public long getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public long getMaxIdleTime() {
        return maxIdleTime;
    }
    
    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }
    
    public long getRequestTimeout() {
        return requestTimeout;
    }
    
    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }
    
    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }
    
    public void setHeartbeatInterval(long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }
    
    public String getSerializerType() {
        return serializerType;
    }
    
    public void setSerializerType(String serializerType) {
        this.serializerType = serializerType;
    }
    
    public boolean isEnableCompression() {
        return enableCompression;
    }
    
    public void setEnableCompression(boolean enableCompression) {
        this.enableCompression = enableCompression;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public long getRetryDelay() {
        return retryDelay;
    }
    
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }
    
    public String getLoadBalanceStrategy() {
        return loadBalanceStrategy;
    }
    
    public void setLoadBalanceStrategy(String loadBalanceStrategy) {
        this.loadBalanceStrategy = loadBalanceStrategy;
    }
    
    public boolean isEnableMetrics() {
        return enableMetrics;
    }
    
    public void setEnableMetrics(boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
    }
    
    public boolean isEnableHealthCheck() {
        return enableHealthCheck;
    }
    
    public void setEnableHealthCheck(boolean enableHealthCheck) {
        this.enableHealthCheck = enableHealthCheck;
    }
    
    public long getMetricsInterval() {
        return metricsInterval;
    }
    
    public void setMetricsInterval(long metricsInterval) {
        this.metricsInterval = metricsInterval;
    }
    
    public String getLogLevel() {
        return logLevel;
    }
    
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
    
    public boolean isEnableRequestLog() {
        return enableRequestLog;
    }
    
    public void setEnableRequestLog(boolean enableRequestLog) {
        this.enableRequestLog = enableRequestLog;
    }
    
    public boolean isEnableResponseLog() {
        return enableResponseLog;
    }
    
    public void setEnableResponseLog(boolean enableResponseLog) {
        this.enableResponseLog = enableResponseLog;
    }
    
    public boolean isEnableSsl() {
        return enableSsl;
    }
    
    public void setEnableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
    }
    
    public String getSslCertPath() {
        return sslCertPath;
    }
    
    public void setSslCertPath(String sslCertPath) {
        this.sslCertPath = sslCertPath;
    }
    
    public String getSslKeyPath() {
        return sslKeyPath;
    }
    
    public void setSslKeyPath(String sslKeyPath) {
        this.sslKeyPath = sslKeyPath;
    }
    
    public int getCoreThreads() {
        return coreThreads;
    }
    
    public void setCoreThreads(int coreThreads) {
        this.coreThreads = coreThreads;
    }
    
    public int getMaxThreads() {
        return maxThreads;
    }
    
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }
    
    public long getThreadKeepAliveTime() {
        return threadKeepAliveTime;
    }
    
    public void setThreadKeepAliveTime(long threadKeepAliveTime) {
        this.threadKeepAliveTime = threadKeepAliveTime;
    }
    
    public int getThreadQueueSize() {
        return threadQueueSize;
    }
    
    public void setThreadQueueSize(int threadQueueSize) {
        this.threadQueueSize = threadQueueSize;
    }
    
    @Override
    public String toString() {
        return "RpcConfig{" +
                "maxConnections=" + maxConnections +
                ", minConnections=" + minConnections +
                ", connectionTimeout=" + connectionTimeout +
                ", requestTimeout=" + requestTimeout +
                ", serializerType='" + serializerType + '\'' +
                ", maxRetries=" + maxRetries +
                ", loadBalanceStrategy='" + loadBalanceStrategy + '\'' +
                ", enableMetrics=" + enableMetrics +
                ", coreThreads=" + coreThreads +
                ", maxThreads=" + maxThreads +
                '}';
    }
} 