using System;

namespace EF.RPC.Sharing
{
    /// <summary>
    /// 序列化接口
    /// 遵循统一架构设计规范，为RPC框架提供标准化的序列化功能
    /// </summary>
    public interface ISerializer
    {
        /// <summary>
        /// 序列化对象
        /// </summary>
        /// <param name="obj">要序列化的对象</param>
        /// <returns>序列化后的字节数组</returns>
        byte[] Serialize(object obj);
        
        /// <summary>
        /// 反序列化对象
        /// </summary>
        /// <typeparam name="T">目标类型</typeparam>
        /// <param name="data">序列化的字节数组</param>
        /// <returns>反序列化后的对象</returns>
        T Deserialize<T>(byte[] data);
        
        /// <summary>
        /// 反序列化对象（指定类型）
        /// </summary>
        /// <param name="data">序列化的字节数组</param>
        /// <param name="type">目标类型</param>
        /// <returns>反序列化后的对象</returns>
        object Deserialize(byte[] data, Type type);
        
        /// <summary>
        /// 序列化对象为字符串
        /// </summary>
        /// <param name="obj">要序列化的对象</param>
        /// <returns>序列化后的字符串</returns>
        string SerializeToString(object obj);
        
        /// <summary>
        /// 从字符串反序列化对象
        /// </summary>
        /// <typeparam name="T">目标类型</typeparam>
        /// <param name="data">序列化的字符串</param>
        /// <returns>反序列化后的对象</returns>
        T DeserializeFromString<T>(string data);
        
        /// <summary>
        /// 从字符串反序列化对象（指定类型）
        /// </summary>
        /// <param name="data">序列化的字符串</param>
        /// <param name="type">目标类型</param>
        /// <returns>反序列化后的对象</returns>
        object DeserializeFromString(string data, Type type);
        
        /// <summary>
        /// 获取序列化器名称
        /// </summary>
        string SerializerName { get; }
        
        /// <summary>
        /// 获取序列化器版本
        /// </summary>
        string SerializerVersion { get; }
        
        /// <summary>
        /// 获取支持的内容类型
        /// </summary>
        string ContentType { get; }
        
        /// <summary>
        /// 检查是否支持指定类型
        /// </summary>
        /// <param name="type">要检查的类型</param>
        /// <returns>true表示支持，false表示不支持</returns>
        bool SupportsType(Type type);
        
        /// <summary>
        /// 获取序列化器描述
        /// </summary>
        string Description { get; }
        
        /// <summary>
        /// 设置序列化器配置
        /// </summary>
        /// <param name="config">配置对象</param>
        void SetConfig(IRpcConfig config);
        
        /// <summary>
        /// 获取序列化器配置
        /// </summary>
        /// <returns>配置对象</returns>
        IRpcConfig GetConfig();
        
        /// <summary>
        /// 获取序列化后的大小
        /// </summary>
        /// <param name="obj">要序列化的对象</param>
        /// <returns>序列化后的大小（字节）</returns>
        int GetSerializedSize(object obj);
        
        /// <summary>
        /// 检查是否启用压缩
        /// </summary>
        bool IsCompressionEnabled { get; }
        
        /// <summary>
        /// 设置是否启用压缩
        /// </summary>
        /// <param name="enabled">是否启用</param>
        void SetCompressionEnabled(bool enabled);
        
        /// <summary>
        /// 检查是否启用缓存
        /// </summary>
        bool IsCacheEnabled { get; }
        
        /// <summary>
        /// 设置是否启用缓存
        /// </summary>
        /// <param name="enabled">是否启用</param>
        void SetCacheEnabled(bool enabled);
        
        /// <summary>
        /// 清除缓存
        /// </summary>
        void ClearCache();
        
        /// <summary>
        /// 获取缓存统计信息
        /// </summary>
        /// <returns>缓存统计信息</returns>
        CacheStats GetCacheStats();
    }
    
    /// <summary>
    /// 缓存统计信息
    /// </summary>
    public class CacheStats
    {
        /// <summary>
        /// 缓存命中次数
        /// </summary>
        public long HitCount { get; set; }
        
        /// <summary>
        /// 缓存未命中次数
        /// </summary>
        public long MissCount { get; set; }
        
        /// <summary>
        /// 缓存大小
        /// </summary>
        public int CacheSize { get; set; }
        
        /// <summary>
        /// 最大缓存大小
        /// </summary>
        public int MaxCacheSize { get; set; }
        
        /// <summary>
        /// 缓存命中率
        /// </summary>
        public double HitRate
        {
            get
            {
                long total = HitCount + MissCount;
                return total > 0 ? (double)HitCount / total : 0.0;
            }
        }
        
        /// <summary>
        /// 缓存使用率
        /// </summary>
        public double UsageRate
        {
            get
            {
                return MaxCacheSize > 0 ? (double)CacheSize / MaxCacheSize : 0.0;
            }
        }
    }
} 