package com.wiqer.rpc.impl.core.improved;

/**
 * RPC服务接口
 * 改进设计：标记接口，用于标识RPC服务实现类
 */
public interface RpcService extends RpcComponent {
    
    /**
     * 获取服务名称
     */
    String getServiceName();
    
    /**
     * 获取服务版本
     */
    String getServiceVersion();
    
    /**
     * 获取服务接口
     */
    Class<?> getServiceInterface();
    
    /**
     * 获取服务实现类
     */
    Class<?> getServiceImplementation();
    
    /**
     * 注册服务方法
     */
    void registerMethod(String methodName, RpcMethod method);
    
    /**
     * 获取服务方法
     */
    RpcMethod getMethod(String methodName);
    
    /**
     * 获取所有服务方法
     */
    java.util.Map<String, RpcMethod> getAllMethods();
} 