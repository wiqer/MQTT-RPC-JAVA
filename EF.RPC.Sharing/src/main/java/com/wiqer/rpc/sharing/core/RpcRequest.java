package com.wiqer.rpc.sharing.core;

/**
 * RPC请求消息
 * 遵循统一架构设计规范，定义标准化的RPC请求格式
 * 
 * @author EFRPC Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class RpcRequest extends RpcMessage {
    
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
    
    /**
     * 构造函数
     * 
     * @param serviceName 服务名称
     * @param methodName 方法名称
     * @param version 版本号
     * @param arguments 方法参数
     */
    public RpcRequest(String serviceName, String methodName, String version, Object[] arguments) {
        super();
        setMessageType(MessageType.REQUEST);
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.version = version;
        this.arguments = arguments;
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
     * 设置服务名称
     * 
     * @param serviceName 服务名称
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
     * 设置方法名称
     * 
     * @param methodName 方法名称
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    /**
     * 获取版本号
     * 
     * @return 版本号
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * 设置版本号
     * 
     * @param version 版本号
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * 获取方法参数
     * 
     * @return 方法参数
     */
    public Object[] getArguments() {
        return arguments;
    }
    
    /**
     * 设置方法参数
     * 
     * @param arguments 方法参数
     */
    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }
    
    /**
     * 获取参数类型
     * 
     * @return 参数类型
     */
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }
    
    /**
     * 设置参数类型
     * 
     * @param parameterTypes 参数类型
     */
    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
    
    /**
     * 获取返回类型
     * 
     * @return 返回类型
     */
    public Class<?> getReturnType() {
        return returnType;
    }
    
    /**
     * 设置返回类型
     * 
     * @param returnType 返回类型
     */
    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }
    
    /**
     * 获取服务键（用于路由）
     * 
     * @return 服务键
     */
    public String getServiceKey() {
        return serviceName + ":" + version;
    }
    
    /**
     * 获取方法键（用于缓存）
     * 
     * @return 方法键
     */
    public String getMethodKey() {
        return serviceName + "." + methodName + ":" + version;
    }
    
    /**
     * 检查是否有参数
     * 
     * @return true表示有参数，false表示无参数
     */
    public boolean hasArguments() {
        return arguments != null && arguments.length > 0;
    }
    
    /**
     * 获取参数数量
     * 
     * @return 参数数量
     */
    public int getArgumentCount() {
        return arguments != null ? arguments.length : 0;
    }
    
    @Override
    public String toString() {
        return String.format("RpcRequest{messageId='%s', serviceName='%s', methodName='%s', version='%s', " +
                "arguments=%d, timestamp=%d}",
                getMessageId(), serviceName, methodName, version, 
                getArgumentCount(), getTimestamp());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        
        RpcRequest that = (RpcRequest) obj;
        
        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        
        // 比较参数数组
        if (arguments == null ? that.arguments != null : that.arguments == null) return false;
        if (arguments != null && that.arguments != null) {
            if (arguments.length != that.arguments.length) return false;
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i] == null ? that.arguments[i] != null : !arguments[i].equals(that.arguments[i])) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        
        // 计算参数数组的哈希码
        if (arguments != null) {
            for (Object arg : arguments) {
                result = 31 * result + (arg != null ? arg.hashCode() : 0);
            }
        }
        
        return result;
    }
    
    /**
     * 创建RPC请求的构建器
     * 
     * @return 请求构建器
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * RPC请求构建器
     */
    public static class Builder {
        private String messageId;
        private String serviceName;
        private String methodName;
        private String version;
        private Object[] arguments;
        private Class<?>[] parameterTypes;
        private Class<?> returnType;
        
        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }
        
        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }
        
        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }
        
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        public Builder arguments(Object[] arguments) {
            this.arguments = arguments;
            return this;
        }
        
        public Builder parameterTypes(Class<?>[] parameterTypes) {
            this.parameterTypes = parameterTypes;
            return this;
        }
        
        public Builder returnType(Class<?> returnType) {
            this.returnType = returnType;
            return this;
        }
        
        public RpcRequest build() {
            RpcRequest request = new RpcRequest(messageId, serviceName, methodName, version);
            request.setArguments(arguments);
            request.setParameterTypes(parameterTypes);
            request.setReturnType(returnType);
            return request;
        }
    }
} 