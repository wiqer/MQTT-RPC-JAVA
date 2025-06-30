package com.wiqer.rpc.sharing.core;

import java.util.List;
import java.util.Map;

/**
 * RPC服务端接口
 * 定义RPC服务端的功能，包括服务注册、统计信息等
 * 
 * @author EFRPC Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface IRpcServer extends IRpcComponent {
    
    /**
     * 获取服务端名称
     * 
     * @return 服务端名称
     */
    String getServerName();
    
    /**
     * 获取服务端版本
     * 
     * @return 服务端版本号
     */
    String getServerVersion();
    
    /**
     * 注册服务实现
     * 
     * @param serviceImpl 服务实现对象
     * @throws RpcException 注册失败时抛出异常
     */
    void registerService(Object serviceImpl) throws RpcException;
    
    /**
     * 注册服务实现（带配置）
     * 
     * @param serviceImpl 服务实现对象
     * @param config 服务配置
     * @throws RpcException 注册失败时抛出异常
     */
    void registerService(Object serviceImpl, IRpcConfig config) throws RpcException;
    
    /**
     * 注册服务实现（指定服务名和版本）
     * 
     * @param serviceName 服务名称
     * @param version 服务版本
     * @param serviceImpl 服务实现对象
     * @throws RpcException 注册失败时抛出异常
     */
    void registerService(String serviceName, String version, Object serviceImpl) throws RpcException;
    
    /**
     * 注册服务实现（指定服务名、版本和配置）
     * 
     * @param serviceName 服务名称
     * @param version 服务版本
     * @param serviceImpl 服务实现对象
     * @param config 服务配置
     * @throws RpcException 注册失败时抛出异常
     */
    void registerService(String serviceName, String version, Object serviceImpl, IRpcConfig config) throws RpcException;
    
    /**
     * 注销服务
     * 
     * @param serviceName 服务名称
     * @param version 服务版本
     * @throws RpcException 注销失败时抛出异常
     */
    void unregisterService(String serviceName, String version) throws RpcException;
    
    /**
     * 获取已注册的服务列表
     * 
     * @return 服务信息列表
     */
    List<ServiceInfo> getRegisteredServices();
    
    /**
     * 检查服务是否已注册
     * 
     * @param serviceName 服务名称
     * @param version 服务版本
     * @return true表示已注册，false表示未注册
     */
    boolean isServiceRegistered(String serviceName, String version);
    
    /**
     * 获取服务端配置
     * 
     * @return 服务端配置
     */
    IRpcConfig getConfig();
    
    /**
     * 设置服务端配置
     * 
     * @param config 服务端配置
     */
    void setConfig(IRpcConfig config);
    
    /**
     * 获取服务端统计信息
     * 
     * @return 服务端统计信息
     */
    ServerStats getStats();
    
    /**
     * 服务信息
     */
    class ServiceInfo {
        private final String serviceName;
        private final String version;
        private final String description;
        private final String author;
        private final long registerTime;
        private final Map<String, String> metadata;
        
        public ServiceInfo(String serviceName, String version, String description, String author,
                          long registerTime, Map<String, String> metadata) {
            this.serviceName = serviceName;
            this.version = version;
            this.description = description;
            this.author = author;
            this.registerTime = registerTime;
            this.metadata = metadata;
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
        
        public long getRegisterTime() {
            return registerTime;
        }
        
        public Map<String, String> getMetadata() {
            return metadata;
        }
        
        @Override
        public String toString() {
            return String.format("ServiceInfo{serviceName='%s', version='%s', description='%s', " +
                    "author='%s', registerTime=%d, metadata=%s}",
                    serviceName, version, description, author, registerTime, metadata);
        }
    }
    
    /**
     * 服务端统计信息
     */
    class ServerStats {
        private final long totalRequests;
        private final long successfulRequests;
        private final long failedRequests;
        private final long averageResponseTime;
        private final long uptime;
        private final long maxResponseTime;
        private final long minResponseTime;
        private final double qps;
        private final int activeConnections;
        private final int totalConnections;
        
        public ServerStats(long totalRequests, long successfulRequests, long failedRequests,
                          long averageResponseTime, long uptime, long maxResponseTime, long minResponseTime,
                          int activeConnections, int totalConnections) {
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.averageResponseTime = averageResponseTime;
            this.uptime = uptime;
            this.maxResponseTime = maxResponseTime;
            this.minResponseTime = minResponseTime;
            this.qps = uptime > 0 ? (double) totalRequests / (uptime / 1000.0) : 0.0;
            this.activeConnections = activeConnections;
            this.totalConnections = totalConnections;
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
        
        public int getActiveConnections() {
            return activeConnections;
        }
        
        public int getTotalConnections() {
            return totalConnections;
        }
        
        public double getSuccessRate() {
            return totalRequests > 0 ? (double) successfulRequests / totalRequests : 0.0;
        }
        
        public double getErrorRate() {
            return totalRequests > 0 ? (double) failedRequests / totalRequests : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("ServerStats{totalRequests=%d, successfulRequests=%d, failedRequests=%d, " +
                    "averageResponseTime=%dms, uptime=%dms, maxResponseTime=%dms, minResponseTime=%dms, " +
                    "qps=%.2f, successRate=%.2f%%, errorRate=%.2f%%, activeConnections=%d, totalConnections=%d}",
                    totalRequests, successfulRequests, failedRequests, averageResponseTime, uptime,
                    maxResponseTime, minResponseTime, qps, getSuccessRate() * 100, getErrorRate() * 100,
                    activeConnections, totalConnections);
        }
    }
} 