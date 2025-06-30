package com.wiqer.rpc.sharing.core;

/**
 * RPC统一异常类
 * 定义标准化的异常处理，包含错误代码、错误类型等信息
 * 
 * @author EFRPC Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class RpcException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    private final String errorType;
    private final long timestamp;
    private final String requestId;
    private final String serviceName;
    private final String methodName;
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public RpcException(String message) {
        this(message, RpcErrorCodes.RPC_ERROR, "UNKNOWN");
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因异常
     */
    public RpcException(String message, Throwable cause) {
        this(message, RpcErrorCodes.RPC_ERROR, "UNKNOWN", cause);
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param errorType 错误类型
     */
    public RpcException(String message, String errorCode, String errorType) {
        this(message, errorCode, errorType, null);
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param errorType 错误类型
     * @param cause 原因异常
     */
    public RpcException(String message, String errorCode, String errorType, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.timestamp = System.currentTimeMillis();
        this.requestId = null;
        this.serviceName = null;
        this.methodName = null;
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param errorType 错误类型
     * @param requestId 请求ID
     * @param serviceName 服务名称
     * @param methodName 方法名称
     */
    public RpcException(String message, String errorCode, String errorType, 
                       String requestId, String serviceName, String methodName) {
        this(message, errorCode, errorType, requestId, serviceName, methodName, null);
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param errorType 错误类型
     * @param requestId 请求ID
     * @param serviceName 服务名称
     * @param methodName 方法名称
     * @param cause 原因异常
     */
    public RpcException(String message, String errorCode, String errorType, 
                       String requestId, String serviceName, String methodName, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.timestamp = System.currentTimeMillis();
        this.requestId = requestId;
        this.serviceName = serviceName;
        this.methodName = methodName;
    }
    
    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 获取错误类型
     * 
     * @return 错误类型
     */
    public String getErrorType() {
        return errorType;
    }
    
    /**
     * 获取时间戳
     * 
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 获取请求ID
     * 
     * @return 请求ID
     */
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * 获取服务名称
     * 
     * @return 服务名称
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * 获取方法名称
     * 
     * @return 方法名称
     */
    public String getMethodName() {
        return methodName;
    }
    
    /**
     * 设置请求ID
     * 
     * @param requestId 请求ID
     * @return 新的异常实例
     */
    public RpcException withRequestId(String requestId) {
        return new RpcException(getMessage(), errorCode, errorType, requestId, serviceName, methodName, getCause());
    }
    
    /**
     * 设置服务信息
     * 
     * @param serviceName 服务名称
     * @param methodName 方法名称
     * @return 新的异常实例
     */
    public RpcException withServiceInfo(String serviceName, String methodName) {
        return new RpcException(getMessage(), errorCode, errorType, requestId, serviceName, methodName, getCause());
    }
    
    @Override
    public String toString() {
        return String.format("RpcException{errorCode='%s', errorType='%s', message='%s', " +
                "timestamp=%d, requestId='%s', serviceName='%s', methodName='%s'}",
                errorCode, errorType, getMessage(), timestamp, requestId, serviceName, methodName);
    }
} 