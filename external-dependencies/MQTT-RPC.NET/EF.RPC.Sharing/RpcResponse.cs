using System;

namespace EF.RPC.Sharing
{
    /// <summary>
    /// RPC响应消息
    /// 遵循统一架构设计规范，定义标准化的RPC响应格式
    /// </summary>
    public class RpcResponse : RpcMessage
    {
        /// <summary>
        /// 请求消息ID
        /// </summary>
        public string RequestId { get; set; }
        
        /// <summary>
        /// 调用结果
        /// </summary>
        public object Result { get; set; }
        
        /// <summary>
        /// 异常信息
        /// </summary>
        public Exception Exception { get; set; }
        
        /// <summary>
        /// 响应状态码
        /// </summary>
        public int StatusCode { get; set; }
        
        /// <summary>
        /// 响应状态消息
        /// </summary>
        public string StatusMessage { get; set; }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        public RpcResponse() : base()
        {
            MessageType = MessageType.Response;
            StatusCode = 200;
            StatusMessage = "OK";
        }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="messageId">消息ID</param>
        /// <param name="requestId">请求消息ID</param>
        public RpcResponse(string messageId, string requestId) 
            : base(messageId, MessageType.Response)
        {
            RequestId = requestId;
            StatusCode = 200;
            StatusMessage = "OK";
        }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="requestId">请求消息ID</param>
        /// <param name="result">调用结果</param>
        public RpcResponse(string requestId, object result) 
            : base()
        {
            MessageType = MessageType.Response;
            RequestId = requestId;
            Result = result;
            StatusCode = 200;
            StatusMessage = "OK";
        }
        
        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="requestId">请求消息ID</param>
        /// <param name="exception">异常信息</param>
        public RpcResponse(string requestId, Exception exception) 
            : base()
        {
            MessageType = MessageType.Response;
            RequestId = requestId;
            Exception = exception;
            StatusCode = 500;
            StatusMessage = "Internal Server Error";
        }
        
        /// <summary>
        /// 检查是否有异常
        /// </summary>
        /// <returns>true表示有异常，false表示无异常</returns>
        public bool HasException()
        {
            return Exception != null;
        }
        
        /// <summary>
        /// 检查是否成功
        /// </summary>
        /// <returns>true表示成功，false表示失败</returns>
        public bool IsSuccess()
        {
            return !HasException() && StatusCode >= 200 && StatusCode < 300;
        }
        
        /// <summary>
        /// 检查是否超时
        /// </summary>
        /// <returns>true表示超时，false表示未超时</returns>
        public bool IsTimeout()
        {
            return StatusCode == 408;
        }
        
        /// <summary>
        /// 检查是否服务不可用
        /// </summary>
        /// <returns>true表示服务不可用，false表示服务可用</returns>
        public bool IsServiceUnavailable()
        {
            return StatusCode == 503;
        }
        
        /// <summary>
        /// 创建成功响应
        /// </summary>
        /// <param name="requestId">请求消息ID</param>
        /// <param name="result">调用结果</param>
        /// <returns>RPC响应</returns>
        public static RpcResponse Success(string requestId, object result)
        {
            return new RpcResponse(requestId, result);
        }
        
        /// <summary>
        /// 创建失败响应
        /// </summary>
        /// <param name="requestId">请求消息ID</param>
        /// <param name="exception">异常信息</param>
        /// <returns>RPC响应</returns>
        public static RpcResponse Failure(string requestId, Exception exception)
        {
            return new RpcResponse(requestId, exception);
        }
        
        /// <summary>
        /// 创建超时响应
        /// </summary>
        /// <param name="requestId">请求消息ID</param>
        /// <returns>RPC响应</returns>
        public static RpcResponse Timeout(string requestId)
        {
            var response = new RpcResponse();
            response.RequestId = requestId;
            response.StatusCode = 408;
            response.StatusMessage = "Request Timeout";
            return response;
        }
        
        /// <summary>
        /// 创建服务不可用响应
        /// </summary>
        /// <param name="requestId">请求消息ID</param>
        /// <returns>RPC响应</returns>
        public static RpcResponse ServiceUnavailable(string requestId)
        {
            var response = new RpcResponse();
            response.RequestId = requestId;
            response.StatusCode = 503;
            response.StatusMessage = "Service Unavailable";
            return response;
        }
        
        /// <summary>
        /// 重写ToString方法
        /// </summary>
        /// <returns>字符串表示</returns>
        public override string ToString()
        {
            return $"RpcResponse{{MessageId='{MessageId}', RequestId='{RequestId}', Success={IsSuccess()}, StatusCode={StatusCode}, Timestamp={Timestamp}}}";
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
            
            var other = (RpcResponse)obj;
            
            return base.Equals(obj) &&
                   RequestId == other.RequestId &&
                   Equals(Result, other.Result) &&
                   Equals(Exception, other.Exception) &&
                   StatusCode == other.StatusCode &&
                   StatusMessage == other.StatusMessage;
        }
        
        /// <summary>
        /// 重写GetHashCode方法
        /// </summary>
        /// <returns>哈希码</returns>
        public override int GetHashCode()
        {
            int result = base.GetHashCode();
            result = 31 * result + (RequestId?.GetHashCode() ?? 0);
            result = 31 * result + (Result?.GetHashCode() ?? 0);
            result = 31 * result + (Exception?.GetHashCode() ?? 0);
            result = 31 * result + StatusCode;
            result = 31 * result + (StatusMessage?.GetHashCode() ?? 0);
            return result;
        }
        
        /// <summary>
        /// 创建RPC响应的构建器
        /// </summary>
        /// <returns>响应构建器</returns>
        public static Builder CreateBuilder()
        {
            return new Builder();
        }
        
        /// <summary>
        /// RPC响应构建器
        /// </summary>
        public class Builder
        {
            private string messageId;
            private string requestId;
            private object result;
            private Exception exception;
            private int statusCode = 200;
            private string statusMessage = "OK";
            
            public Builder SetMessageId(string messageId)
            {
                this.messageId = messageId;
                return this;
            }
            
            public Builder SetRequestId(string requestId)
            {
                this.requestId = requestId;
                return this;
            }
            
            public Builder SetResult(object result)
            {
                this.result = result;
                return this;
            }
            
            public Builder SetException(Exception exception)
            {
                this.exception = exception;
                if (exception != null)
                {
                    this.statusCode = 500;
                    this.statusMessage = "Internal Server Error";
                }
                return this;
            }
            
            public Builder SetStatusCode(int statusCode)
            {
                this.statusCode = statusCode;
                return this;
            }
            
            public Builder SetStatusMessage(string statusMessage)
            {
                this.statusMessage = statusMessage;
                return this;
            }
            
            public RpcResponse Build()
            {
                var response = new RpcResponse(messageId, requestId)
                {
                    Result = result,
                    Exception = exception,
                    StatusCode = statusCode,
                    StatusMessage = statusMessage
                };
                return response;
            }
        }
    }
} 