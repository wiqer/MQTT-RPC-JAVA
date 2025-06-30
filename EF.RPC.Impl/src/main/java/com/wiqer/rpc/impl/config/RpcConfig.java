package com.wiqer.rpc.impl.config;

import lombok.Data;

/**
 * RPC配置类 - 统一管理配置参数
 * 参考.NET版本的配置管理机制
 */
@Data
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
        config.setMaxConnections(200);
        config.setMinConnections(20);
        config.setCoreThreads(32);
        config.setMaxThreads(64);
        config.setThreadQueueSize(2000);
        config.setSerializerType("protobuf");
        config.setEnableCompression(true);
        return config;
    }
    
    /**
     * 创建低延迟配置
     */
    public static RpcConfig createLowLatency() {
        RpcConfig config = new RpcConfig();
        config.setRequestTimeout(5000); // 5秒
        config.setConnectionTimeout(10000); // 10秒
        config.setHeartbeatInterval(10000); // 10秒
        config.setSerializerType("protobuf");
        config.setEnableCompression(false);
        return config;
    }
    
    /**
     * 验证配置
     */
    public void validate() {
        if (maxConnections <= 0) {
            throw new IllegalArgumentException("maxConnections must be positive");
        }
        if (minConnections < 0) {
            throw new IllegalArgumentException("minConnections must be non-negative");
        }
        if (minConnections > maxConnections) {
            throw new IllegalArgumentException("minConnections cannot be greater than maxConnections");
        }
        if (connectionTimeout <= 0) {
            throw new IllegalArgumentException("connectionTimeout must be positive");
        }
        if (requestTimeout <= 0) {
            throw new IllegalArgumentException("requestTimeout must be positive");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be non-negative");
        }
        if (coreThreads <= 0) {
            throw new IllegalArgumentException("coreThreads must be positive");
        }
        if (maxThreads < coreThreads) {
            throw new IllegalArgumentException("maxThreads cannot be less than coreThreads");
        }
    }
} 