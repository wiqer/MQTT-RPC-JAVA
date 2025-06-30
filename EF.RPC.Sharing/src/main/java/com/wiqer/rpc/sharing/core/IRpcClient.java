package com.wiqer.rpc.sharing.core;

import java.util.concurrent.CompletableFuture;

/**
 * RPC客户端接口
 * 定义RPC客户端的功能，包括服务代理创建、统计信息等
 * 
 * @author EFRPC Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface IRpcClient extends IRpcComponent {
    
    /**
     * 获取客户端名称
     * 
     * @return 客户端名称
     */
    String getClientName();
    
    /**
     * 获取客户端版本
     * 
     * @return 客户端版本号
     */
    String getClientVersion();
    
    /**
     * 创建服务代理
     * 
     * @param serviceInterface 服务接口类
     * @param serviceName 服务名称
     * @param version 服务版本
     * @param <T> 服务接口类型
     * @return 服务代理对象
     * @throws RpcException 创建代理失败时抛出异常
     */
    <T> T createProxy(Class<T> serviceInterface, String serviceName, String version) throws RpcException;
    
    /**
     * 创建服务代理（带配置）
     * 
     * @param serviceInterface 服务接口类
     * @param serviceName 服务名称
     * @param version 服务版本
     * @param config 客户端配置
     * @param <T> 服务接口类型
     * @return 服务代理对象
     * @throws RpcException 创建代理失败时抛出异常
     */
    <T> T createProxy(Class<T> serviceInterface, String serviceName, String version, IRpcConfig config) throws RpcException;
    
    /**
     * 调用远程方法
     * 
     * @param serviceName 服务名称
     * @param methodName 方法名称
     * @param args 方法参数
     * @return 调用结果
     * @throws RpcException 调用失败时抛出
     */
    Object invoke(String serviceName, String methodName, Object[] args) throws RpcException;
    
    /**
     * 调用远程方法（带超时）
     * 
     * @param serviceName 服务名称
     * @param methodName 方法名称
     * @param args 方法参数
     * @param timeout 超时时间（毫秒）
     * @return 调用结果
     * @throws RpcException 调用失败时抛出
     */
    Object invoke(String serviceName, String methodName, Object[] args, long timeout) throws RpcException;
    
    /**
     * 异步调用远程方法
     * 
     * @param serviceName 服务名称
     * @param methodName 方法名称
     * @param args 方法参数
     * @return 异步调用结果
     * @throws RpcException 调用失败时抛出
     */
    CompletableFuture<Object> invokeAsync(String serviceName, String methodName, Object[] args) throws RpcException;
    
    /**
     * 异步调用远程方法（带超时）
     * 
     * @param serviceName 服务名称
     * @param methodName 方法名称
     * @param args 方法参数
     * @param timeout 超时时间（毫秒）
     * @return 异步调用结果
     * @throws RpcException 调用失败时抛出
     */
    CompletableFuture<Object> invokeAsync(String serviceName, String methodName, Object[] args, long timeout) throws RpcException;
    
    /**
     * 检查服务是否可用
     * 
     * @param serviceName 服务名称
     * @param version 服务版本
     * @return true表示可用，false表示不可用
     */
    boolean isServiceAvailable(String serviceName, String version);
    
    /**
     * 获取客户端配置
     * 
     * @return 客户端配置
     */
    IRpcConfig getConfig();
    
    /**
     * 设置客户端配置
     * 
     * @param config 客户端配置
     */
    void setConfig(IRpcConfig config);
    
    /**
     * 获取连接状态
     * 
     * @return 连接状态
     */
    ConnectionStatus getConnectionStatus();
    
    /**
     * 获取客户端统计信息
     * 
     * @return 客户端统计信息
     */
    ClientStats getStats();
    
    /**
     * 连接状态枚举
     */
    enum ConnectionStatus {
        /**
         * 未连接
         */
        DISCONNECTED,
        
        /**
         * 连接中
         */
        CONNECTING,
        
        /**
         * 已连接
         */
        CONNECTED,
        
        /**
         * 连接失败
         */
        CONNECTION_FAILED,
        
        /**
         * 重连中
         */
        RECONNECTING
    }
    
    /**
     * 客户端统计信息
     */
    class ClientStats {
        private final long totalRequests;
        private final long successfulRequests;
        private final long failedRequests;
        private final long averageResponseTime;
        private final long uptime;
        private final long maxResponseTime;
        private final long minResponseTime;
        private final double qps;
        
        public ClientStats(long totalRequests, long successfulRequests, long failedRequests,
                          long averageResponseTime, long uptime, long maxResponseTime, long minResponseTime) {
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.averageResponseTime = averageResponseTime;
            this.uptime = uptime;
            this.maxResponseTime = maxResponseTime;
            this.minResponseTime = minResponseTime;
            this.qps = uptime > 0 ? (double) totalRequests / (uptime / 1000.0) : 0.0;
        }
        
        public long getTotalRequests() {
            return totalRequests;
        }
        
        public long getSuccessfulRequests() {
            return successfulRequests;
        }
        
        public long getFailedRequests() {
            return failedRequests;
        }
        
        public long getAverageResponseTime() {
            return averageResponseTime;
        }
        
        public long getUptime() {
            return uptime;
        }
        
        public long getMaxResponseTime() {
            return maxResponseTime;
        }
        
        public long getMinResponseTime() {
            return minResponseTime;
        }
        
        public double getQps() {
            return qps;
        }
        
        public double getSuccessRate() {
            return totalRequests > 0 ? (double) successfulRequests / totalRequests : 0.0;
        }
        
        public double getErrorRate() {
            return totalRequests > 0 ? (double) failedRequests / totalRequests : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("ClientStats{totalRequests=%d, successfulRequests=%d, failedRequests=%d, " +
                    "averageResponseTime=%dms, uptime=%dms, maxResponseTime=%dms, minResponseTime=%dms, " +
                    "qps=%.2f, successRate=%.2f%%, errorRate=%.2f%%}",
                    totalRequests, successfulRequests, failedRequests, averageResponseTime, uptime,
                    maxResponseTime, minResponseTime, qps, getSuccessRate() * 100, getErrorRate() * 100);
        }
    }
} 