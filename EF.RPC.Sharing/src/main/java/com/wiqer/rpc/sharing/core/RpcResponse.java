package com.wiqer.rpc.sharing.core;

/**
 * RPC响应消息
 * 遵循统一架构设计规范，定义标准化的RPC响应格式
 * 
 * @author EFRPC Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class RpcResponse extends RpcMessage {
    
    /**
     * 请求消息ID
     */
    private String requestId;
    
    /**
     * 调用结果
     */
    private Object result;
    
    /**
     * 异常信息
     */
    private Throwable exception;
    
    /**
     * 响应状态码
     */
    private int statusCode;
    
    /**
     * 响应状态消息
     */
    private String statusMessage;
    
    /**
     * 构造函数
     */
    public RpcResponse() {
        super();
        setMessageType(MessageType.RESPONSE);
        this.statusCode = 200;
        this.statusMessage = "OK";
    }
    
    /**
     * 构造函数
     * 
     * @param messageId 消息ID
     * @param requestId 请求消息ID
     */
    public RpcResponse(String messageId, String requestId) {
        super(messageId, MessageType.RESPONSE);
        this.requestId = requestId;
        this.statusCode = 200;
        this.statusMessage = "OK";
    }
    
    /**
     * 构造函数
     * 
     * @param requestId 请求消息ID
     * @param result 调用结果
     */
    public RpcResponse(String requestId, Object result) {
        super();
        setMessageType(MessageType.RESPONSE);
        this.requestId = requestId;
        this.result = result;
        this.statusCode = 200;
        this.statusMessage = "OK";
    }
    
    /**
     * 构造函数
     * 
     * @param requestId 请求消息ID
     * @param exception 异常信息
     */
    public RpcResponse(String requestId, Throwable exception) {
        super();
        setMessageType(MessageType.RESPONSE);
        this.requestId = requestId;
        this.exception = exception;
        this.statusCode = 500;
        this.statusMessage = "Internal Server Error";
    }
    
    /**
     * 获取请求消息ID
     * 
     * @return 请求消息ID
     */
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * 设置请求消息ID
     * 
     * @param requestId 请求消息ID
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    /**
     * 获取调用结果
     * 
     * @return 调用结果
     */
    public Object getResult() {
        return result;
    }
    
    /**
     * 设置调用结果
     * 
     * @param result 调用结果
     */
    public void setResult(Object result) {
        this.result = result;
    }
    
    /**
     * 获取异常信息
     * 
     * @return 异常信息
     */
    public Throwable getException() {
        return exception;
    }
    
    /**
     * 设置异常信息
     * 
     * @param exception 异常信息
     */
    public void setException(Throwable exception) {
        this.exception = exception;
        if (exception != null) {
            this.statusCode = 500;
            this.statusMessage = "Internal Server Error";
        }
    }
    
    /**
     * 获取响应状态码
     * 
     * @return 响应状态码
     */
    public int getStatusCode() {
        return statusCode;
    }
    
    /**
     * 设置响应状态码
     * 
     * @param statusCode 响应状态码
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    /**
     * 获取响应状态消息
     * 
     * @return 响应状态消息
     */
    public String getStatusMessage() {
        return statusMessage;
    }
    
    /**
     * 设置响应状态消息
     * 
     * @param statusMessage 响应状态消息
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    /**
     * 检查是否有异常
     * 
     * @return true表示有异常，false表示无异常
     */
    public boolean hasException() {
        return exception != null;
    }
    
    /**
     * 检查是否成功
     * 
     * @return true表示成功，false表示失败
     */
    public boolean isSuccess() {
        return !hasException() && statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * 检查是否超时
     * 
     * @return true表示超时，false表示未超时
     */
    public boolean isTimeout() {
        return statusCode == 408;
    }
    
    /**
     * 检查是否服务不可用
     * 
     * @return true表示服务不可用，false表示服务可用
     */
    public boolean isServiceUnavailable() {
        return statusCode == 503;
    }
    
    /**
     * 创建成功响应
     * 
     * @param requestId 请求消息ID
     * @param result 调用结果
     * @return RPC响应
     */
    public static RpcResponse success(String requestId, Object result) {
        return new RpcResponse(requestId, result);
    }
    
    /**
     * 创建失败响应
     * 
     * @param requestId 请求消息ID
     * @param exception 异常信息
     * @return RPC响应
     */
    public static RpcResponse failure(String requestId, Throwable exception) {
        return new RpcResponse(requestId, exception);
    }
    
    /**
     * 创建超时响应
     * 
     * @param requestId 请求消息ID
     * @return RPC响应
     */
    public static RpcResponse timeout(String requestId) {
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setStatusCode(408);
        response.setStatusMessage("Request Timeout");
        return response;
    }
    
    /**
     * 创建服务不可用响应
     * 
     * @param requestId 请求消息ID
     * @return RPC响应
     */
    public static RpcResponse serviceUnavailable(String requestId) {
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setStatusCode(503);
        response.setStatusMessage("Service Unavailable");
        return response;
    }
    
    @Override
    public String toString() {
        return String.format("RpcResponse{messageId='%s', requestId='%s', success=%s, statusCode=%d, timestamp=%d}",
                getMessageId(), requestId, isSuccess(), statusCode, getTimestamp());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        
        RpcResponse that = (RpcResponse) obj;
        
        if (statusCode != that.statusCode) return false;
        if (requestId != null ? !requestId.equals(that.requestId) : that.requestId != null) return false;
        if (result != null ? !result.equals(that.result) : that.result != null) return false;
        if (exception != null ? !exception.equals(that.exception) : that.exception != null) return false;
        return statusMessage != null ? statusMessage.equals(that.statusMessage) : that.statusMessage == null;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (requestId != null ? requestId.hashCode() : 0);
        result = 31 * result + (this.result != null ? this.result.hashCode() : 0);
        result = 31 * result + (exception != null ? exception.hashCode() : 0);
        result = 31 * result + statusCode;
        result = 31 * result + (statusMessage != null ? statusMessage.hashCode() : 0);
        return result;
    }
    
    /**
     * 创建RPC响应的构建器
     * 
     * @return 响应构建器
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * RPC响应构建器
     */
    public static class Builder {
        private String messageId;
        private String requestId;
        private Object result;
        private Throwable exception;
        private int statusCode = 200;
        private String statusMessage = "OK";
        
        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }
        
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }
        
        public Builder result(Object result) {
            this.result = result;
            return this;
        }
        
        public Builder exception(Throwable exception) {
            this.exception = exception;
            if (exception != null) {
                this.statusCode = 500;
                this.statusMessage = "Internal Server Error";
            }
            return this;
        }
        
        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }
        
        public Builder statusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }
        
        public RpcResponse build() {
            RpcResponse response = new RpcResponse(messageId, requestId);
            response.setResult(result);
            response.setException(exception);
            response.setStatusCode(statusCode);
            response.setStatusMessage(statusMessage);
            return response;
        }
    }
} 