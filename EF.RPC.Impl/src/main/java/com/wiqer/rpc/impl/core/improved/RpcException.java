package com.wiqer.rpc.impl.core.improved;

/**
 * RPC异常类
 * 改进设计：统一的异常处理，提供详细的错误信息
 */
public class RpcException extends RuntimeException {
    
    private final String errorCode;
    private final String serviceName;
    private final String methodName;
    private final long timestamp;
    
    public RpcException(String message) {
        super(message);
        this.errorCode = ErrorCodes.GENERAL_ERROR;
        this.serviceName = null;
        this.methodName = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    public RpcException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCodes.GENERAL_ERROR;
        this.serviceName = null;
        this.methodName = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    public RpcException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.serviceName = null;
        this.methodName = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    public RpcException(String errorCode, String message, String serviceName, String methodName) {
        super(message);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.timestamp = System.currentTimeMillis();
    }
    
    public RpcException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.serviceName = null;
        this.methodName = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    public RpcException(String errorCode, String message, String serviceName, String methodName, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RpcException{");
        sb.append("errorCode='").append(errorCode).append('\'');
        if (serviceName != null) {
            sb.append(", serviceName='").append(serviceName).append('\'');
        }
        if (methodName != null) {
            sb.append(", methodName='").append(methodName).append('\'');
        }
        sb.append(", message='").append(getMessage()).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }
    
    /**
     * 预定义的错误码
     */
    public static class ErrorCodes {
        public static final String GENERAL_ERROR = "RPC_GENERAL_ERROR";
        public static final String TIMEOUT = "RPC_TIMEOUT";
        public static final String SERVICE_NOT_FOUND = "SERVICE_NOT_FOUND";
        public static final String METHOD_NOT_FOUND = "METHOD_NOT_FOUND";
        public static final String SERIALIZATION_ERROR = "SERIALIZATION_ERROR";
        public static final String NETWORK_ERROR = "NETWORK_ERROR";
        public static final String INVOCATION_ERROR = "INVOCATION_ERROR";
        public static final String CONNECTION_ERROR = "CONNECTION_ERROR";
        public static final String CONFIG_ERROR = "CONFIG_ERROR";
        public static final String AUTHENTICATION_ERROR = "AUTHENTICATION_ERROR";
        public static final String AUTHORIZATION_ERROR = "AUTHORIZATION_ERROR";
        public static final String RATE_LIMIT_ERROR = "RATE_LIMIT_ERROR";
        public static final String CIRCUIT_BREAKER_ERROR = "CIRCUIT_BREAKER_ERROR";
    }
    
    /**
     * 创建超时异常
     */
    public static RpcException timeout(String serviceName, String methodName, long timeout) {
        return new RpcException(
            ErrorCodes.TIMEOUT,
            String.format("RPC call timeout after %dms", timeout),
            serviceName,
            methodName
        );
    }
    
    /**
     * 创建服务未找到异常
     */
    public static RpcException serviceNotFound(String serviceName) {
        return new RpcException(
            ErrorCodes.SERVICE_NOT_FOUND,
            String.format("Service '%s' not found", serviceName),
            serviceName,
            null
        );
    }
    
    /**
     * 创建方法未找到异常
     */
    public static RpcException methodNotFound(String serviceName, String methodName) {
        return new RpcException(
            ErrorCodes.METHOD_NOT_FOUND,
            String.format("Method '%s' not found in service '%s'", methodName, serviceName),
            serviceName,
            methodName
        );
    }
    
    /**
     * 创建序列化异常
     */
    public static RpcException serializationError(String message, Throwable cause) {
        return new RpcException(ErrorCodes.SERIALIZATION_ERROR, message, cause);
    }
    
    /**
     * 创建网络异常
     */
    public static RpcException networkError(String message, Throwable cause) {
        return new RpcException(ErrorCodes.NETWORK_ERROR, message, cause);
    }
    
    /**
     * 创建调用异常
     */
    public static RpcException invocationError(String serviceName, String methodName, Throwable cause) {
        return new RpcException(
            ErrorCodes.INVOCATION_ERROR,
            String.format("Failed to invoke method '%s' in service '%s'", methodName, serviceName),
            serviceName,
            methodName,
            cause
        );
    }
    
    /**
     * 创建连接异常
     */
    public static RpcException connectionError(String message, Throwable cause) {
        return new RpcException(ErrorCodes.CONNECTION_ERROR, message, cause);
    }
    
    /**
     * 创建配置异常
     */
    public static RpcException configError(String message) {
        return new RpcException(ErrorCodes.CONFIG_ERROR, message);
    }
} 