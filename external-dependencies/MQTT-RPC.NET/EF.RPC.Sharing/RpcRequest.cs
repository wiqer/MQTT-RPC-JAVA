using System;

namespace EF.RPC.Sharing
{
    /// <summary>
    /// RPC请求消息
    /// 遵循统一架构设计规范，定义标准化的RPC请求格式
    /// </summary>
    public class RpcRequest : RpcMessage
    {
        /// <summary>
        /// 服务名称
        /// </summary>
        public string ServiceName { get; set; }
        
        /// <summary>
        /// 方法名称
        /// </summary>
        public string MethodName { get; set; }
        
        /// <summary>
        /// 版本号
        /// </summary>
        public string Version { get; set; }
        
        /// <summary>
        /// 方法参数
        /// </summary>
        public object[] Arguments { get; set; }
        
        /// <summary>
        /// 参数类型
        /// </summary>
        public Type[] ParameterTypes { get; set; }
        
        /// <summary>
        /// 返回类型
        /// </summary>
        public Type ReturnType { get; set; }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        public RpcRequest() : base()
        {
            MessageType = MessageType.Request;
        }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="messageId">消息ID</param>
        /// <param name="serviceName">服务名称</param>
        /// <param name="methodName">方法名称</param>
        /// <param name="version">版本号</param>
        public RpcRequest(string messageId, string serviceName, string methodName, string version) 
            : base(messageId, MessageType.Request)
        {
            ServiceName = serviceName;
            MethodName = methodName;
            Version = version;
        }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="methodName">方法名称</param>
        /// <param name="version">版本号</param>
        /// <param name="arguments">方法参数</param>
        public RpcRequest(string serviceName, string methodName, string version, object[] arguments) 
            : base()
        {
            MessageType = MessageType.Request;
            ServiceName = serviceName;
            MethodName = methodName;
            Version = version;
            Arguments = arguments;
        }
        
        /// <summary>
        /// 获取服务键（用于路由）
        /// </summary>
        /// <returns>服务键</returns>
        public string GetServiceKey()
        {
            return $"{ServiceName}:{Version}";
        }
        
        /// <summary>
        /// 获取方法键（用于缓存）
        /// </summary>
        /// <returns>方法键</returns>
        public string GetMethodKey()
        {
            return $"{ServiceName}.{MethodName}:{Version}";
        }
        
        /// <summary>
        /// 检查是否有参数
        /// </summary>
        /// <returns>true表示有参数，false表示无参数</returns>
        public bool HasArguments()
        {
            return Arguments != null && Arguments.Length > 0;
        }
        
        /// <summary>
        /// 获取参数数量
        /// </summary>
        /// <returns>参数数量</returns>
        public int GetArgumentCount()
        {
            return Arguments != null ? Arguments.Length : 0;
        }
        
        /// <summary>
        /// 重写ToString方法
        /// </summary>
        /// <returns>字符串表示</returns>
        public override string ToString()
        {
            return $"RpcRequest{{MessageId='{MessageId}', ServiceName='{ServiceName}', MethodName='{MethodName}', Version='{Version}', Arguments={GetArgumentCount()}, Timestamp={Timestamp}}}";
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
            
            var other = (RpcRequest)obj;
            
            return base.Equals(obj) &&
                   ServiceName == other.ServiceName &&
                   MethodName == other.MethodName &&
                   Version == other.Version &&
                   Equals(Arguments, other.Arguments);
        }
        
        /// <summary>
        /// 重写GetHashCode方法
        /// </summary>
        /// <returns>哈希码</returns>
        public override int GetHashCode()
        {
            int result = base.GetHashCode();
            result = 31 * result + (ServiceName?.GetHashCode() ?? 0);
            result = 31 * result + (MethodName?.GetHashCode() ?? 0);
            result = 31 * result + (Version?.GetHashCode() ?? 0);
            
            // 计算参数数组的哈希码
            if (Arguments != null)
            {
                foreach (var arg in Arguments)
                {
                    result = 31 * result + (arg?.GetHashCode() ?? 0);
                }
            }
            
            return result;
        }
        
        /// <summary>
        /// 创建RPC请求的构建器
        /// </summary>
        /// <returns>请求构建器</returns>
        public static Builder CreateBuilder()
        {
            return new Builder();
        }
        
        /// <summary>
        /// RPC请求构建器
        /// </summary>
        public class Builder
        {
            private string messageId;
            private string serviceName;
            private string methodName;
            private string version;
            private object[] arguments;
            private Type[] parameterTypes;
            private Type returnType;
            
            public Builder SetMessageId(string messageId)
            {
                this.messageId = messageId;
                return this;
            }
            
            public Builder SetServiceName(string serviceName)
            {
                this.serviceName = serviceName;
                return this;
            }
            
            public Builder SetMethodName(string methodName)
            {
                this.methodName = methodName;
                return this;
            }
            
            public Builder SetVersion(string version)
            {
                this.version = version;
                return this;
            }
            
            public Builder SetArguments(object[] arguments)
            {
                this.arguments = arguments;
                return this;
            }
            
            public Builder SetParameterTypes(Type[] parameterTypes)
            {
                this.parameterTypes = parameterTypes;
                return this;
            }
            
            public Builder SetReturnType(Type returnType)
            {
                this.returnType = returnType;
                return this;
            }
            
            public RpcRequest Build()
            {
                var request = new RpcRequest(messageId, serviceName, methodName, version)
                {
                    Arguments = arguments,
                    ParameterTypes = parameterTypes,
                    ReturnType = returnType
                };
                return request;
            }
        }
    }
} 