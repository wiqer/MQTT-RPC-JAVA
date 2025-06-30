package com.wiqer.rpc.sharing.core;

/**
 * RPC服务接口
 * 遵循统一架构设计规范，为RPC服务提供标准化的定义
 * 
 * @author EFRPC Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface IRpcService extends IRpcComponent {
    
    /**
     * 获取服务名称
     * 
     * @return 服务名称
     */
    String getServiceName();
    
    /**
     * 获取服务版本
     * 
     * @return 服务版本号
     */
    String getServiceVersion();
    
    /**
     * 获取服务接口类型
     * 
     * @return 服务接口类型
     */
    Class<?> getServiceInterface();
    
    /**
     * 获取服务实现类型
     * 
     * @return 服务实现类型
     */
    Class<?> getServiceImplementation();
    
    /**
     * 注册服务方法
     * 
     * @param methodName 方法名称
     * @param method 方法信息
     */
    void registerMethod(String methodName, RpcMethod method);
    
    /**
     * 获取服务方法
     * 
     * @param methodName 方法名称
     * @return 方法信息，如果不存在返回null
     */
    RpcMethod getMethod(String methodName);
    
    /**
     * 获取所有服务方法
     * 
     * @return 所有方法信息的映射
     */
    java.util.Map<String, RpcMethod> getAllMethods();
    
    /**
     * 检查服务是否可用
     * 
     * @return true表示可用，false表示不可用
     */
    boolean isAvailable();
    
    /**
     * 获取服务元数据
     * 
     * @return 服务元数据
     */
    ServiceMetadata getMetadata();
    
    /**
     * RPC方法信息
     */
    class RpcMethod {
        private final String name;
        private final String version;
        private final java.lang.reflect.Method method;
        private final Class<?>[] parameterTypes;
        private final Class<?> returnType;
        private final long timeout;
        private final boolean async;
        
        public RpcMethod(String name, String version, java.lang.reflect.Method method, 
                        long timeout, boolean async) {
            this.name = name;
            this.version = version;
            this.method = method;
            this.parameterTypes = method.getParameterTypes();
            this.returnType = method.getReturnType();
            this.timeout = timeout;
            this.async = async;
        }
        
        public String getName() {
            return name;
        }
        
        public String getVersion() {
            return version;
        }
        
        public java.lang.reflect.Method getMethod() {
            return method;
        }
        
        public Class<?>[] getParameterTypes() {
            return parameterTypes;
        }
        
        public Class<?> getReturnType() {
            return returnType;
        }
        
        public long getTimeout() {
            return timeout;
        }
        
        public boolean isAsync() {
            return async;
        }
        
        @Override
        public String toString() {
            return String.format("RpcMethod{name='%s', version='%s', timeout=%d, async=%s}",
                    name, version, timeout, async);
        }
    }
    
    /**
     * 服务元数据
     */
    class ServiceMetadata {
        private final String serviceName;
        private final String version;
        private final String description;
        private final String author;
        private final long createTime;
        private final java.util.Map<String, Object> properties;
        
        public ServiceMetadata(String serviceName, String version, String description, 
                             String author, java.util.Map<String, Object> properties) {
            this.serviceName = serviceName;
            this.version = version;
            this.description = description;
            this.author = author;
            this.createTime = System.currentTimeMillis();
            this.properties = properties != null ? new java.util.HashMap<>(properties) : new java.util.HashMap<>();
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public String getVersion() {
            return version;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getAuthor() {
            return author;
        }
        
        public long getCreateTime() {
            return createTime;
        }
        
        public java.util.Map<String, Object> getProperties() {
            return new java.util.HashMap<>(properties);
        }
        
        public Object getProperty(String key) {
            return properties.get(key);
        }
        
        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }
        
        @Override
        public String toString() {
            return String.format("ServiceMetadata{serviceName='%s', version='%s', description='%s', author='%s', createTime=%d}",
                    serviceName, version, description, author, createTime);
        }
    }
} 