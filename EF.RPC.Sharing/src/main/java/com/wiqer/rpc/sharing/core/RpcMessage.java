package com.wiqer.rpc.sharing.core;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC消息基类
 * 遵循统一架构设计规范，为所有RPC消息提供标准化的基础结构
 * 
 * @author EFRPC Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class RpcMessage {
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 消息类型
     */
    private MessageType messageType;
    
    /**
     * 时间戳
     */
    private long timestamp;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 构造函数
     */
    public RpcMessage() {
        this.messageId = generateMessageId();
        this.timestamp = System.currentTimeMillis();
        this.metadata = new HashMap<>();
    }
    
    /**
     * 构造函数
     * 
     * @param messageId 消息ID
     * @param messageType 消息类型
     */
    public RpcMessage(String messageId, MessageType messageType) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.timestamp = System.currentTimeMillis();
        this.metadata = new HashMap<>();
    }
    
    /**
     * 生成消息ID
     * 
     * @return 消息ID
     */
    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }
    
    /**
     * 获取消息ID
     * 
     * @return 消息ID
     */
    public String getMessageId() {
        return messageId;
    }
    
    /**
     * 设置消息ID
     * 
     * @param messageId 消息ID
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    /**
     * 获取消息类型
     * 
     * @return 消息类型
     */
    public MessageType getMessageType() {
        return messageType;
    }
    
    /**
     * 设置消息类型
     * 
     * @param messageType 消息类型
     */
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
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
     * 设置时间戳
     * 
     * @param timestamp 时间戳
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * 获取元数据
     * 
     * @return 元数据
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    /**
     * 设置元数据
     * 
     * @param metadata 元数据
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    /**
     * 添加元数据
     * 
     * @param key 键
     * @param value 值
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    /**
     * 获取元数据
     * 
     * @param key 键
     * @return 值
     */
    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        /**
         * 请求消息
         */
        REQUEST,
        
        /**
         * 响应消息
         */
        RESPONSE,
        
        /**
         * 心跳消息
         */
        HEARTBEAT,
        
        /**
         * 错误消息
         */
        ERROR
    }
    
    @Override
    public String toString() {
        return String.format("RpcMessage{messageId='%s', messageType=%s, timestamp=%d, metadata=%s}",
                messageId, messageType, timestamp, metadata);
    }
}

/**
 * RPC请求消息
 */
class RpcRequest extends RpcMessage {
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 方法名称
     */
    private String methodName;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 方法参数
     */
    private Object[] arguments;
    
    /**
     * 参数类型
     */
    private Class<?>[] parameterTypes;
    
    /**
     * 返回类型
     */
    private Class<?> returnType;
    
    /**
     * 构造函数
     */
    public RpcRequest() {
        super();
        setMessageType(MessageType.REQUEST);
    }
    
    /**
     * 构造函数
     * 
     * @param messageId 消息ID
     * @param serviceName 服务名称
     * @param methodName 方法名称
     * @param version 版本号
     */
    public RpcRequest(String messageId, String serviceName, String methodName, String version) {
        super(messageId, MessageType.REQUEST);
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.version = version;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Object[] getArguments() {
        return arguments;
    }
    
    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }
    
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }
    
    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
    
    public Class<?> getReturnType() {
        return returnType;
    }
    
    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }
    
    @Override
    public String toString() {
        return String.format("RpcRequest{messageId='%s', serviceName='%s', methodName='%s', version='%s', " +
                "arguments=%d, timestamp=%d}",
                getMessageId(), serviceName, methodName, version, 
                arguments != null ? arguments.length : 0, getTimestamp());
    }
}

/**
 * RPC响应消息
 */
class RpcResponse extends RpcMessage {
    
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
     * 构造函数
     */
    public RpcResponse() {
        super();
        setMessageType(MessageType.RESPONSE);
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
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public Object getResult() {
        return result;
    }
    
    public void setResult(Object result) {
        this.result = result;
    }
    
    public Throwable getException() {
        return exception;
    }
    
    public void setException(Throwable exception) {
        this.exception = exception;
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
        return !hasException();
    }
    
    @Override
    public String toString() {
        return String.format("RpcResponse{messageId='%s', requestId='%s', success=%s, timestamp=%d}",
                getMessageId(), requestId, isSuccess(), getTimestamp());
    }
}

/**
 * RPC心跳消息
 */
class RpcHeartbeat extends RpcMessage {
    
    /**
     * 客户端ID
     */
    private String clientId;
    
    /**
     * 服务端ID
     */
    private String serverId;
    
    /**
     * 构造函数
     */
    public RpcHeartbeat() {
        super();
        setMessageType(MessageType.HEARTBEAT);
    }
    
    /**
     * 构造函数
     * 
     * @param clientId 客户端ID
     * @param serverId 服务端ID
     */
    public RpcHeartbeat(String clientId, String serverId) {
        super();
        setMessageType(MessageType.HEARTBEAT);
        this.clientId = clientId;
        this.serverId = serverId;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getServerId() {
        return serverId;
    }
    
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
    
    @Override
    public String toString() {
        return String.format("RpcHeartbeat{messageId='%s', clientId='%s', serverId='%s', timestamp=%d}",
                getMessageId(), clientId, serverId, getTimestamp());
    }
}

/**
 * RPC错误消息
 */
class RpcErrorMessage extends RpcMessage {
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    /**
     * 错误详情
     */
    private String errorDetails;
    
    /**
     * 构造函数
     */
    public RpcErrorMessage() {
        super();
        setMessageType(MessageType.ERROR);
    }
    
    /**
     * 构造函数
     * 
     * @param errorCode 错误代码
     * @param errorMessage 错误消息
     */
    public RpcErrorMessage(String errorCode, String errorMessage) {
        super();
        setMessageType(MessageType.ERROR);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getErrorDetails() {
        return errorDetails;
    }
    
    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
    
    @Override
    public String toString() {
        return String.format("RpcErrorMessage{messageId='%s', errorCode='%s', errorMessage='%s', timestamp=%d}",
                getMessageId(), errorCode, errorMessage, getTimestamp());
    }
} 