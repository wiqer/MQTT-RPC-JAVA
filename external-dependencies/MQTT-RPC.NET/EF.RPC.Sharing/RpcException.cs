using System;

namespace EF.RPC.Sharing
{
    /// <summary>
    /// RPC框架统一异常类
    /// 遵循统一架构设计规范，提供标准化的异常处理机制
    /// </summary>
    public class RpcException : Exception
    {
        /// <summary>
        /// 错误代码
        /// </summary>
        public string ErrorCode { get; }
        
        /// <summary>
        /// 服务名称
        /// </summary>
        public string ServiceName { get; }
        
        /// <summary>
        /// 方法名称
        /// </summary>
        public string MethodName { get; }
        
        /// <summary>
        /// 异常发生时间戳
        /// </summary>
        public long Timestamp { get; }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="message">异常消息</param>
        public RpcException(string message) : base(message)
        {
            ErrorCode = "RPC_ERROR";
            ServiceName = null;
            MethodName = null;
            Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="message">异常消息</param>
        /// <param name="innerException">内部异常</param>
        public RpcException(string message, Exception innerException) : base(message, innerException)
        {
            ErrorCode = "RPC_ERROR";
            ServiceName = null;
            MethodName = null;
            Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="errorCode">错误代码</param>
        /// <param name="message">异常消息</param>
        public RpcException(string errorCode, string message) : base(message)
        {
            ErrorCode = errorCode;
            ServiceName = null;
            MethodName = null;
            Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="errorCode">错误代码</param>
        /// <param name="message">异常消息</param>
        /// <param name="serviceName">服务名称</param>
        /// <param name="methodName">方法名称</param>
        public RpcException(string errorCode, string message, string serviceName, string methodName) : base(message)
        {
            ErrorCode = errorCode;
            ServiceName = serviceName;
            MethodName = methodName;
            Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="errorCode">错误代码</param>
        /// <param name="message">异常消息</param>
        /// <param name="innerException">内部异常</param>
        public RpcException(string errorCode, string message, Exception innerException) : base(message, innerException)
        {
            ErrorCode = errorCode;
            ServiceName = null;
            MethodName = null;
            Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="errorCode">错误代码</param>
        /// <param name="message">异常消息</param>
        /// <param name="serviceName">服务名称</param>
        /// <param name="methodName">方法名称</param>
        /// <param name="innerException">内部异常</param>
        public RpcException(string errorCode, string message, string serviceName, string methodName, Exception innerException) 
            : base(message, innerException)
        {
            ErrorCode = errorCode;
            ServiceName = serviceName;
            MethodName = methodName;
            Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        }
        
        /// <summary>
        /// 重写ToString方法
        /// </summary>
        /// <returns>异常信息字符串</returns>
        public override string ToString()
        {
            return $"RpcException{{ErrorCode='{ErrorCode}', ServiceName='{ServiceName}', MethodName='{MethodName}', Timestamp={Timestamp}, Message='{Message}'}}";
        }
    }
} 