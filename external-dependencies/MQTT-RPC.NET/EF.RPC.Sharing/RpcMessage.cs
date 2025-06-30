using System;
using System.Collections.Generic;

namespace EF.RPC.Sharing
{
    /// <summary>
    /// RPC消息基类
    /// 遵循统一架构设计规范，定义标准化的RPC消息格式
    /// </summary>
    public abstract class RpcMessage
    {
        /// <summary>
        /// 消息ID
        /// </summary>
        public string MessageId { get; set; }
        
        /// <summary>
        /// 消息类型
        /// </summary>
        public MessageType MessageType { get; set; }
        
        /// <summary>
        /// 时间戳
        /// </summary>
        public long Timestamp { get; set; }
        
        /// <summary>
        /// 元数据
        /// </summary>
        public Dictionary<string, object> Metadata { get; set; }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        protected RpcMessage()
        {
            MessageId = Guid.NewGuid().ToString();
            Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
            Metadata = new Dictionary<string, object>();
        }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="messageId">消息ID</param>
        /// <param name="messageType">消息类型</param>
        protected RpcMessage(string messageId, MessageType messageType)
        {
            MessageId = messageId ?? Guid.NewGuid().ToString();
            MessageType = messageType;
            Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
            Metadata = new Dictionary<string, object>();
        }
        
        /// <summary>
        /// 添加元数据
        /// </summary>
        /// <param name="key">键</param>
        /// <param name="value">值</param>
        public void AddMetadata(string key, object value)
        {
            if (Metadata == null)
            {
                Metadata = new Dictionary<string, object>();
            }
            Metadata[key] = value;
        }
        
        /// <summary>
        /// 获取元数据
        /// </summary>
        /// <param name="key">键</param>
        /// <returns>值</returns>
        public object GetMetadata(string key)
        {
            return Metadata?.ContainsKey(key) == true ? Metadata[key] : null;
        }
        
        /// <summary>
        /// 检查是否包含指定元数据
        /// </summary>
        /// <param name="key">键</param>
        /// <returns>true表示包含，false表示不包含</returns>
        public bool ContainsMetadata(string key)
        {
            return Metadata?.ContainsKey(key) == true;
        }
        
        /// <summary>
        /// 移除元数据
        /// </summary>
        /// <param name="key">键</param>
        /// <returns>true表示移除成功，false表示移除失败</returns>
        public bool RemoveMetadata(string key)
        {
            return Metadata?.Remove(key) == true;
        }
        
        /// <summary>
        /// 清除所有元数据
        /// </summary>
        public void ClearMetadata()
        {
            Metadata?.Clear();
        }
        
        /// <summary>
        /// 重写ToString方法
        /// </summary>
        /// <returns>字符串表示</returns>
        public override string ToString()
        {
            return $"RpcMessage{{MessageId='{MessageId}', MessageType={MessageType}, Timestamp={Timestamp}, Metadata={Metadata?.Count ?? 0}}}";
        }
        
        /// <summary>
        /// 重写Equals方法
        /// </summary>
        /// <param name="obj">比较对象</param>
        /// <returns>是否相等</returns>
        public override bool Equals(object obj)
        {
            if (obj == null || GetType() != obj.GetType())
                return false;
            
            if (ReferenceEquals(this, obj))
                return true;
            
            var other = (RpcMessage)obj;
            
            return MessageId == other.MessageId &&
                   MessageType == other.MessageType &&
                   Timestamp == other.Timestamp;
        }
        
        /// <summary>
        /// 重写GetHashCode方法
        /// </summary>
        /// <returns>哈希码</returns>
        public override int GetHashCode()
        {
            int result = MessageId?.GetHashCode() ?? 0;
            result = 31 * result + MessageType.GetHashCode();
            result = 31 * result + Timestamp.GetHashCode();
            return result;
        }
    }
    
    /// <summary>
    /// 消息类型枚举
    /// </summary>
    public enum MessageType
    {
        /// <summary>
        /// 请求消息
        /// </summary>
        Request,
        
        /// <summary>
        /// 响应消息
        /// </summary>
        Response,
        
        /// <summary>
        /// 心跳消息
        /// </summary>
        Heartbeat,
        
        /// <summary>
        /// 错误消息
        /// </summary>
        Error
    }
} 