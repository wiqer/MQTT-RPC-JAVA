using System;
using System.Collections.Generic;

namespace EF.RPC.Sharing
{
    /// <summary>
    /// RPC配置接口
    /// 遵循统一架构设计规范，为RPC组件提供标准化的配置管理
    /// </summary>
    public interface IRpcConfig
    {
        /// <summary>
        /// 获取配置名称
        /// </summary>
        string ConfigName { get; }
        
        /// <summary>
        /// 获取配置版本
        /// </summary>
        string ConfigVersion { get; }
        
        /// <summary>
        /// 获取或设置超时时间（毫秒）
        /// </summary>
        long Timeout { get; set; }
        
        /// <summary>
        /// 获取或设置重试次数
        /// </summary>
        int RetryCount { get; set; }
        
        /// <summary>
        /// 获取或设置重试间隔（毫秒）
        /// </summary>
        long RetryInterval { get; set; }
        
        /// <summary>
        /// 获取或设置连接池大小
        /// </summary>
        int ConnectionPoolSize { get; set; }
        
        /// <summary>
        /// 获取或设置最大消息大小（字节）
        /// </summary>
        int MaxMessageSize { get; set; }
        
        /// <summary>
        /// 获取或设置是否启用压缩
        /// </summary>
        bool EnableCompression { get; set; }
        
        /// <summary>
        /// 获取或设置是否启用加密
        /// </summary>
        bool EnableEncryption { get; set; }
        
        /// <summary>
        /// 获取或设置是否启用监控
        /// </summary>
        bool EnableMonitoring { get; set; }
        
        /// <summary>
        /// 获取或设置是否启用日志
        /// </summary>
        bool EnableLogging { get; set; }
        
        /// <summary>
        /// 获取或设置日志级别
        /// </summary>
        string LogLevel { get; set; }
        
        /// <summary>
        /// 获取或设置序列化器类型
        /// </summary>
        string SerializerType { get; set; }
        
        /// <summary>
        /// 获取或设置传输协议
        /// </summary>
        string TransportProtocol { get; set; }
        
        /// <summary>
        /// 获取或设置服务发现地址
        /// </summary>
        string ServiceDiscoveryAddress { get; set; }
        
        /// <summary>
        /// 获取或设置负载均衡策略
        /// </summary>
        string LoadBalanceStrategy { get; set; }
        
        /// <summary>
        /// 获取或设置熔断器配置
        /// </summary>
        CircuitBreakerConfig CircuitBreaker { get; set; }
        
        /// <summary>
        /// 获取或设置限流配置
        /// </summary>
        RateLimitConfig RateLimit { get; set; }
        
        /// <summary>
        /// 获取或设置缓存配置
        /// </summary>
        CacheConfig Cache { get; set; }
        
        /// <summary>
        /// 获取自定义属性
        /// </summary>
        /// <param name="key">属性键</param>
        /// <returns>属性值</returns>
        object GetProperty(string key);
        
        /// <summary>
        /// 设置自定义属性
        /// </summary>
        /// <param name="key">属性键</param>
        /// <param name="value">属性值</param>
        void SetProperty(string key, object value);
        
        /// <summary>
        /// 获取所有自定义属性
        /// </summary>
        /// <returns>属性字典</returns>
        IDictionary<string, object> GetAllProperties();
        
        /// <summary>
        /// 检查是否包含指定属性
        /// </summary>
        /// <param name="key">属性键</param>
        /// <returns>true表示包含，false表示不包含</returns>
        bool ContainsProperty(string key);
        
        /// <summary>
        /// 移除指定属性
        /// </summary>
        /// <param name="key">属性键</param>
        /// <returns>true表示移除成功，false表示移除失败</returns>
        bool RemoveProperty(string key);
        
        /// <summary>
        /// 清除所有自定义属性
        /// </summary>
        void ClearProperties();
        
        /// <summary>
        /// 合并配置
        /// </summary>
        /// <param name="other">其他配置</param>
        void Merge(IRpcConfig other);
        
        /// <summary>
        /// 克隆配置
        /// </summary>
        /// <returns>配置副本</returns>
        IRpcConfig Clone();
    }
    
    /// <summary>
    /// 熔断器配置
    /// </summary>
    public class CircuitBreakerConfig
    {
        /// <summary>
        /// 是否启用熔断器
        /// </summary>
        public bool Enabled { get; set; } = false;
        
        /// <summary>
        /// 失败阈值
        /// </summary>
        public int FailureThreshold { get; set; } = 5;
        
        /// <summary>
        /// 恢复时间（毫秒）
        /// </summary>
        public long RecoveryTime { get; set; } = 60000;
        
        /// <summary>
        /// 半开状态超时时间（毫秒）
        /// </summary>
        public long HalfOpenTimeout { get; set; } = 30000;
    }
    
    /// <summary>
    /// 限流配置
    /// </summary>
    public class RateLimitConfig
    {
        /// <summary>
        /// 是否启用限流
        /// </summary>
        public bool Enabled { get; set; } = false;
        
        /// <summary>
        /// 限流阈值（每秒请求数）
        /// </summary>
        public int RateLimit { get; set; } = 1000;
        
        /// <summary>
        /// 限流窗口大小（秒）
        /// </summary>
        public int WindowSize { get; set; } = 1;
        
        /// <summary>
        /// 限流策略
        /// </summary>
        public string Strategy { get; set; } = "token_bucket";
    }
    
    /// <summary>
    /// 缓存配置
    /// </summary>
    public class CacheConfig
    {
        /// <summary>
        /// 是否启用缓存
        /// </summary>
        public bool Enabled { get; set; } = false;
        
        /// <summary>
        /// 缓存过期时间（秒）
        /// </summary>
        public long ExpireTime { get; set; } = 300;
        
        /// <summary>
        /// 最大缓存大小
        /// </summary>
        public int MaxSize { get; set; } = 1000;
        
        /// <summary>
        /// 缓存策略
        /// </summary>
        public string Strategy { get; set; } = "lru";
    }
} 