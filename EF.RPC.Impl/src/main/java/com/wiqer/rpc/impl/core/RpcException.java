package com.wiqer.rpc.impl.core;

/**
 * RPC异常类 - 统一异常处理
 * 参考.NET版本的异常处理机制
 */
public class RpcException extends RuntimeException {
    
    private final String errorCode;
    private final String serviceName;
    private final String methodName;
    
    public RpcException(String message) {
        super(message);
        this.errorCode = "RPC_ERROR";
        this.serviceName = null;
        this.methodName = null;
    }
    
    public RpcException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "RPC_ERROR";
        this.serviceName = null;
        this.methodName = null;
    }
    
    public RpcException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.serviceName = null;
        this.methodName = null;
    }
    
    public RpcException(String errorCode, String message, String serviceName, String methodName) {
        super(message);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
        this.methodName = methodName;
    }
    
    public RpcException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.serviceName = null;
        this.methodName = null;
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
        sb.append('}');
        return sb.toString();
    }
    
    /**
     * 预定义的错误码
     */
    public static class ErrorCodes {
        public static final String TIMEOUT = "RPC_TIMEOUT";
        public static final String SERVICE_NOT_FOUND = "SERVICE_NOT_FOUND";
        public static final String METHOD_NOT_FOUND = "METHOD_NOT_FOUND";
        public static final String SERIALIZATION_ERROR = "SERIALIZATION_ERROR";
        public static final String NETWORK_ERROR = "NETWORK_ERROR";
        public static final String INVOCATION_ERROR = "INVOCATION_ERROR";
    }
} 