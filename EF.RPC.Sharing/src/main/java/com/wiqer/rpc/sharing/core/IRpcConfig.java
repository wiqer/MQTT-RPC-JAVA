package com.wiqer.rpc.sharing.core;

import java.util.Map;

/**
 * RPC配置接口
 * 遵循统一架构设计规范，为RPC组件提供标准化的配置管理
 * 
 * @author EFRPC Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface IRpcConfig {
    
    /**
     * 获取配置名称
     * 
     * @return 配置名称
     */
    String getConfigName();
    
    /**
     * 获取配置版本
     * 
     * @return 配置版本
     */
    String getConfigVersion();
    
    /**
     * 获取超时时间（毫秒）
     * 
     * @return 超时时间
     */
    long getTimeout();
    
    /**
     * 设置超时时间（毫秒）
     * 
     * @param timeout 超时时间
     */
    void setTimeout(long timeout);
    
    /**
     * 获取重试次数
     * 
     * @return 重试次数
     */
    int getRetryCount();
    
    /**
     * 设置重试次数
     * 
     * @param retryCount 重试次数
     */
    void setRetryCount(int retryCount);
    
    /**
     * 获取重试间隔（毫秒）
     * 
     * @return 重试间隔
     */
    long getRetryInterval();
    
    /**
     * 设置重试间隔（毫秒）
     * 
     * @param retryInterval 重试间隔
     */
    void setRetryInterval(long retryInterval);
    
    /**
     * 获取连接池大小
     * 
     * @return 连接池大小
     */
    int getConnectionPoolSize();
    
    /**
     * 设置连接池大小
     * 
     * @param connectionPoolSize 连接池大小
     */
    void setConnectionPoolSize(int connectionPoolSize);
    
    /**
     * 获取最大消息大小（字节）
     * 
     * @return 最大消息大小
     */
    int getMaxMessageSize();
    
    /**
     * 设置最大消息大小（字节）
     * 
     * @param maxMessageSize 最大消息大小
     */
    void setMaxMessageSize(int maxMessageSize);
    
    /**
     * 是否启用压缩
     * 
     * @return true表示启用，false表示禁用
     */
    boolean isEnableCompression();
    
    /**
     * 设置是否启用压缩
     * 
     * @param enableCompression 是否启用压缩
     */
    void setEnableCompression(boolean enableCompression);
    
    /**
     * 是否启用加密
     * 
     * @return true表示启用，false表示禁用
     */
    boolean isEnableEncryption();
    
    /**
     * 设置是否启用加密
     * 
     * @param enableEncryption 是否启用加密
     */
    void setEnableEncryption(boolean enableEncryption);
    
    /**
     * 是否启用监控
     * 
     * @return true表示启用，false表示禁用
     */
    boolean isEnableMonitoring();
    
    /**
     * 设置是否启用监控
     * 
     * @param enableMonitoring 是否启用监控
     */
    void setEnableMonitoring(boolean enableMonitoring);
    
    /**
     * 是否启用日志
     * 
     * @return true表示启用，false表示禁用
     */
    boolean isEnableLogging();
    
    /**
     * 设置是否启用日志
     * 
     * @param enableLogging 是否启用日志
     */
    void setEnableLogging(boolean enableLogging);
    
    /**
     * 获取日志级别
     * 
     * @return 日志级别
     */
    String getLogLevel();
    
    /**
     * 设置日志级别
     * 
     * @param logLevel 日志级别
     */
    void setLogLevel(String logLevel);
    
    /**
     * 获取序列化器类型
     * 
     * @return 序列化器类型
     */
    String getSerializerType();
    
    /**
     * 设置序列化器类型
     * 
     * @param serializerType 序列化器类型
     */
    void setSerializerType(String serializerType);
    
    /**
     * 获取传输协议
     * 
     * @return 传输协议
     */
    String getTransportProtocol();
    
    /**
     * 设置传输协议
     * 
     * @param transportProtocol 传输协议
     */
    void setTransportProtocol(String transportProtocol);
    
    /**
     * 获取服务发现地址
     * 
     * @return 服务发现地址
     */
    String getServiceDiscoveryAddress();
    
    /**
     * 设置服务发现地址
     * 
     * @param serviceDiscoveryAddress 服务发现地址
     */
    void setServiceDiscoveryAddress(String serviceDiscoveryAddress);
    
    /**
     * 获取负载均衡策略
     * 
     * @return 负载均衡策略
     */
    String getLoadBalanceStrategy();
    
    /**
     * 设置负载均衡策略
     * 
     * @param loadBalanceStrategy 负载均衡策略
     */
    void setLoadBalanceStrategy(String loadBalanceStrategy);
    
    /**
     * 获取熔断器配置
     * 
     * @return 熔断器配置
     */
    CircuitBreakerConfig getCircuitBreaker();
    
    /**
     * 设置熔断器配置
     * 
     * @param circuitBreaker 熔断器配置
     */
    void setCircuitBreaker(CircuitBreakerConfig circuitBreaker);
    
    /**
     * 获取限流配置
     * 
     * @return 限流配置
     */
    RateLimitConfig getRateLimit();
    
    /**
     * 设置限流配置
     * 
     * @param rateLimit 限流配置
     */
    void setRateLimit(RateLimitConfig rateLimit);
    
    /**
     * 获取缓存配置
     * 
     * @return 缓存配置
     */
    CacheConfig getCache();
    
    /**
     * 设置缓存配置
     * 
     * @param cache 缓存配置
     */
    void setCache(CacheConfig cache);
    
    /**
     * 获取自定义属性
     * 
     * @param key 属性键
     * @return 属性值
     */
    Object getProperty(String key);
    
    /**
     * 设置自定义属性
     * 
     * @param key 属性键
     * @param value 属性值
     */
    void setProperty(String key, Object value);
    
    /**
     * 获取所有自定义属性
     * 
     * @return 属性映射
     */
    Map<String, Object> getAllProperties();
    
    /**
     * 检查是否包含指定属性
     * 
     * @param key 属性键
     * @return true表示包含，false表示不包含
     */
    boolean containsProperty(String key);
    
    /**
     * 移除指定属性
     * 
     * @param key 属性键
     * @return true表示移除成功，false表示移除失败
     */
    boolean removeProperty(String key);
    
    /**
     * 清除所有自定义属性
     */
    void clearProperties();
    
    /**
     * 合并配置
     * 
     * @param other 其他配置
     */
    void merge(IRpcConfig other);
    
    /**
     * 克隆配置
     * 
     * @return 配置副本
     */
    IRpcConfig clone();
    
    /**
     * 熔断器配置
     */
    class CircuitBreakerConfig {
        private boolean enabled = false;
        private int failureThreshold = 5;
        private long recoveryTime = 60000;
        private long halfOpenTimeout = 30000;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getFailureThreshold() {
            return failureThreshold;
        }
        
        public void setFailureThreshold(int failureThreshold) {
            this.failureThreshold = failureThreshold;
        }
        
        public long getRecoveryTime() {
            return recoveryTime;
        }
        
        public void setRecoveryTime(long recoveryTime) {
            this.recoveryTime = recoveryTime;
        }
        
        public long getHalfOpenTimeout() {
            return halfOpenTimeout;
        }
        
        public void setHalfOpenTimeout(long halfOpenTimeout) {
            this.halfOpenTimeout = halfOpenTimeout;
        }
    }
    
    /**
     * 限流配置
     */
    class RateLimitConfig {
        private boolean enabled = false;
        private int rateLimit = 1000;
        private int windowSize = 1;
        private String strategy = "token_bucket";
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getRateLimit() {
            return rateLimit;
        }
        
        public void setRateLimit(int rateLimit) {
            this.rateLimit = rateLimit;
        }
        
        public int getWindowSize() {
            return windowSize;
        }
        
        public void setWindowSize(int windowSize) {
            this.windowSize = windowSize;
        }
        
        public String getStrategy() {
            return strategy;
        }
        
        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }
    }
    
    /**
     * 缓存配置
     */
    class CacheConfig {
        private boolean enabled = false;
        private long expireTime = 300;
        private int maxSize = 1000;
        private String strategy = "lru";
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public long getExpireTime() {
            return expireTime;
        }
        
        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }
        
        public int getMaxSize() {
            return maxSize;
        }
        
        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }
        
        public String getStrategy() {
            return strategy;
        }
        
        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }
    }
} 