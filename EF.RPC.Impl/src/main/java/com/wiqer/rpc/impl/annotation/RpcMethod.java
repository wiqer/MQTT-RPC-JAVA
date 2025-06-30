package com.wiqer.rpc.impl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC方法注解
 * 遵循统一架构设计规范，用于标识RPC服务方法
 * 
 * @author EFRPC Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcMethod {
    
    /**
     * 方法名称
     * 
     * @return 方法名称，默认为空字符串（使用原方法名）
     */
    String name() default "";
    
    /**
     * 版本号
     * 
     * @return 版本号，默认为空字符串（使用服务版本）
     */
    String version() default "";
    
    /**
     * 超时时间
     * 
     * @return 超时时间（毫秒），默认为5000
     */
    long timeout() default 5000;
    
    /**
     * 是否异步
     * 
     * @return true表示异步，false表示同步，默认为false
     */
    boolean async() default false;
    
    /**
     * 方法描述
     * 
     * @return 方法描述，默认为空字符串
     */
    String description() default "";
    
    /**
     * 是否启用缓存
     * 
     * @return true表示启用缓存，false表示禁用缓存，默认为false
     */
    boolean enableCache() default false;
    
    /**
     * 缓存过期时间
     * 
     * @return 缓存过期时间（秒），默认为300
     */
    long cacheExpire() default 300;
    
    /**
     * 是否启用重试
     * 
     * @return true表示启用重试，false表示禁用重试，默认为true
     */
    boolean enableRetry() default true;
    
    /**
     * 重试次数
     * 
     * @return 重试次数，默认为3
     */
    int retryCount() default 3;
    
    /**
     * 重试间隔
     * 
     * @return 重试间隔（毫秒），默认为1000
     */
    long retryInterval() default 1000;
    
    /**
     * 是否启用压缩
     * 
     * @return true表示启用压缩，false表示禁用压缩，默认为false
     */
    boolean enableCompression() default false;
    
    /**
     * 是否启用加密
     * 
     * @return true表示启用加密，false表示禁用加密，默认为false
     */
    boolean enableEncryption() default false;
    
    /**
     * 是否启用监控
     * 
     * @return true表示启用监控，false表示禁用监控，默认为true
     */
    boolean enableMonitoring() default true;
    
    /**
     * 是否启用日志
     * 
     * @return true表示启用日志，false表示禁用日志，默认为true
     */
    boolean enableLogging() default true;
    
    /**
     * 日志级别
     * 
     * @return 日志级别，默认为"INFO"
     */
    String logLevel() default "INFO";
    
    /**
     * 序列化器类型
     * 
     * @return 序列化器类型，默认为"json"
     */
    String serializerType() default "json";
    
    /**
     * 传输协议
     * 
     * @return 传输协议，默认为"tcp"
     */
    String transportProtocol() default "tcp";
    
    /**
     * 连接池大小
     * 
     * @return 连接池大小，默认为10
     */
    int connectionPoolSize() default 10;
    
    /**
     * 最大消息大小
     * 
     * @return 最大消息大小（字节），默认为1048576（1MB）
     */
    int maxMessageSize() default 1048576;
    
    /**
     * 是否启用熔断器
     * 
     * @return true表示启用熔断器，false表示禁用熔断器，默认为false
     */
    boolean enableCircuitBreaker() default false;
    
    /**
     * 熔断器失败阈值
     * 
     * @return 熔断器失败阈值，默认为5
     */
    int circuitBreakerFailureThreshold() default 5;
    
    /**
     * 熔断器恢复时间
     * 
     * @return 熔断器恢复时间（毫秒），默认为60000
     */
    long circuitBreakerRecoveryTime() default 60000;
    
    /**
     * 是否启用限流
     * 
     * @return true表示启用限流，false表示禁用限流，默认为false
     */
    boolean enableRateLimit() default false;
    
    /**
     * 限流阈值
     * 
     * @return 限流阈值（每秒请求数），默认为1000
     */
    int rateLimitThreshold() default 1000;
    
    /**
     * 限流窗口大小
     * 
     * @return 限流窗口大小（秒），默认为1
     */
    int rateLimitWindowSize() default 1;
    
    /**
     * 限流策略
     * 
     * @return 限流策略，默认为"token_bucket"
     */
    String rateLimitStrategy() default "token_bucket";
    
    /**
     * 最大缓存大小
     * 
     * @return 最大缓存大小，默认为1000
     */
    int maxCacheSize() default 1000;
    
    /**
     * 缓存策略
     * 
     * @return 缓存策略，默认为"lru"
     */
    String cacheStrategy() default "lru";
    
    /**
     * 自定义属性
     * 
     * @return 自定义属性数组
     */
    Property[] properties() default {};
    
    /**
     * 自定义属性
     */
    @interface Property {
        /**
         * 属性键
         * 
         * @return 属性键
         */
        String key();
        
        /**
         * 属性值
         * 
         * @return 属性值
         */
        String value();
    }
} 