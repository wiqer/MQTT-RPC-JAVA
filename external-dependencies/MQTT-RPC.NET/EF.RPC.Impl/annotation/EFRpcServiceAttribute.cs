﻿using System;
using System.Collections.Generic;
using System.Text;

namespace EF.RPC.Impl.annotation
{
    /// <summary>
    /// RPC服务特性
    /// 遵循统一架构设计规范，用于标识RPC服务实现类
    /// </summary>
    [AttributeUsage(AttributeTargets.Class)]
    public class EFRpcServiceAttribute : Attribute
    {
        /// <summary>
        /// 版本号
        /// </summary>
        public string Version { get; set; } = "v1";
        
        /// <summary>
        /// 服务接口类型
        /// </summary>
        public Type ServiceInterface { get; set; }
        
        /// <summary>
        /// 服务名称
        /// </summary>
        public string ServiceName { get; set; } = "";
        
        /// <summary>
        /// 是否启用
        /// </summary>
        public bool Enabled { get; set; } = true;
        
        /// <summary>
        /// 服务描述
        /// </summary>
        public string Description { get; set; } = "";
        
        /// <summary>
        /// 服务作者
        /// </summary>
        public string Author { get; set; } = "";
        
        /// <summary>
        /// 超时时间（毫秒）
        /// </summary>
        public long Timeout { get; set; } = 5000;
        
        /// <summary>
        /// 是否异步
        /// </summary>
        public bool Async { get; set; } = false;
        
        /// <summary>
        /// 负载均衡策略
        /// </summary>
        public string LoadBalance { get; set; } = "round_robin";
        
        /// <summary>
        /// 重试次数
        /// </summary>
        public int RetryCount { get; set; } = 3;
        
        /// <summary>
        /// 重试间隔（毫秒）
        /// </summary>
        public long RetryInterval { get; set; } = 1000;
        
        /// <summary>
        /// 是否启用压缩
        /// </summary>
        public bool EnableCompression { get; set; } = false;
        
        /// <summary>
        /// 是否启用加密
        /// </summary>
        public bool EnableEncryption { get; set; } = false;
        
        /// <summary>
        /// 是否启用监控
        /// </summary>
        public bool EnableMonitoring { get; set; } = true;
        
        /// <summary>
        /// 是否启用日志
        /// </summary>
        public bool EnableLogging { get; set; } = true;
        
        /// <summary>
        /// 日志级别
        /// </summary>
        public string LogLevel { get; set; } = "INFO";
        
        /// <summary>
        /// 序列化器类型
        /// </summary>
        public string SerializerType { get; set; } = "json";
        
        /// <summary>
        /// 传输协议
        /// </summary>
        public string TransportProtocol { get; set; } = "tcp";
        
        /// <summary>
        /// 服务发现地址
        /// </summary>
        public string ServiceDiscoveryAddress { get; set; } = "";
        
        /// <summary>
        /// 连接池大小
        /// </summary>
        public int ConnectionPoolSize { get; set; } = 10;
        
        /// <summary>
        /// 最大消息大小（字节）
        /// </summary>
        public int MaxMessageSize { get; set; } = 1048576;
        
        /// <summary>
        /// 是否启用熔断器
        /// </summary>
        public bool EnableCircuitBreaker { get; set; } = false;
        
        /// <summary>
        /// 熔断器失败阈值
        /// </summary>
        public int CircuitBreakerFailureThreshold { get; set; } = 5;
        
        /// <summary>
        /// 熔断器恢复时间（毫秒）
        /// </summary>
        public long CircuitBreakerRecoveryTime { get; set; } = 60000;
        
        /// <summary>
        /// 是否启用限流
        /// </summary>
        public bool EnableRateLimit { get; set; } = false;
        
        /// <summary>
        /// 限流阈值（每秒请求数）
        /// </summary>
        public int RateLimitThreshold { get; set; } = 1000;
        
        /// <summary>
        /// 限流窗口大小（秒）
        /// </summary>
        public int RateLimitWindowSize { get; set; } = 1;
        
        /// <summary>
        /// 限流策略
        /// </summary>
        public string RateLimitStrategy { get; set; } = "token_bucket";
        
        /// <summary>
        /// 最大缓存大小
        /// </summary>
        public int MaxCacheSize { get; set; } = 1000;
        
        /// <summary>
        /// 缓存策略
        /// </summary>
        public string CacheStrategy { get; set; } = "lru";
        
        /// <summary>
        /// 自定义属性
        /// </summary>
        public Dictionary<string, string> Properties { get; set; } = new Dictionary<string, string>();
        
        /// <summary>
        /// 策略类型（兼容旧版本）
        /// </summary>
        public Type StrategyType 
        { 
            get => ServiceInterface; 
            set => ServiceInterface = value; 
        }
    }
}
