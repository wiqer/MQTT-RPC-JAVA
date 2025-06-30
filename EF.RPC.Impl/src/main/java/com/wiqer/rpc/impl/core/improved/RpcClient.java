package com.wiqer.rpc.impl.core.improved;

/**
 * RPC客户端接口
 * 改进设计：标记接口，用于标识RPC客户端实现类
 */
public interface RpcClient extends RpcComponent {
    
    /**
     * 获取客户端名称
     */
    String getClientName();
    
    /**
     * 获取客户端版本
     */
    String getClientVersion();
    
    /**
     * 创建服务代理
     */
    <T> T createProxy(Class<T> serviceInterface, String serviceName, String version);
    
    /**
     * 创建服务代理（带配置）
     */
    <T> T createProxy(Class<T> serviceInterface, String serviceName, String version, RpcConfig config);
    
    /**
     * 调用远程方法
     */
    Object invoke(String serviceName, String methodName, Object[] args);
    
    /**
     * 调用远程方法（带超时）
     */
    Object invoke(String serviceName, String methodName, Object[] args, long timeout);
    
    /**
     * 异步调用远程方法
     */
    java.util.concurrent.CompletableFuture<Object> invokeAsync(String serviceName, String methodName, Object[] args);
} 