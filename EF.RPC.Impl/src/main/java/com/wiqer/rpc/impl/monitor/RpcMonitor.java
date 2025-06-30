package com.wiqer.rpc.impl.monitor;

import com.wiqer.rpc.impl.core.ConnectionPool;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * RPC监控器
 * 提供性能监控、健康检查和统计信息
 */
@Slf4j
public class RpcMonitor {
    
    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder totalResponses = new LongAdder();
    private final LongAdder totalErrors = new LongAdder();
    private final LongAdder totalTimeouts = new LongAdder();
    
    private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxResponseTime = new AtomicLong(0);
    private final LongAdder totalResponseTime = new LongAdder();
    
    private final ConcurrentHashMap<String, ServiceStats> serviceStatsMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MethodStats> methodStatsMap = new ConcurrentHashMap<>();
    
    private final ScheduledExecutorService monitorExecutor;
    private final ConnectionPool connectionPool;
    
    public RpcMonitor(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        this.monitorExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rpc-monitor");
            t.setDaemon(true);
            return t;
        });
        
        startMonitoring();
    }
    
    /**
     * 记录请求开始
     */
    public void recordRequestStart(String serviceName, String methodName) {
        totalRequests.increment();
        
        String serviceKey = serviceName;
        String methodKey = serviceName + "." + methodName;
        
        serviceStatsMap.computeIfAbsent(serviceKey, k -> new ServiceStats()).recordRequest();
        methodStatsMap.computeIfAbsent(methodKey, k -> new MethodStats()).recordRequest();
    }
    
    /**
     * 记录请求成功
     */
    public void recordRequestSuccess(String serviceName, String methodName, long responseTime) {
        totalResponses.increment();
        
        String serviceKey = serviceName;
        String methodKey = serviceName + "." + methodName;
        
        // 更新响应时间统计
        updateResponseTimeStats(responseTime);
        
        serviceStatsMap.computeIfAbsent(serviceKey, k -> new ServiceStats()).recordSuccess(responseTime);
        methodStatsMap.computeIfAbsent(methodKey, k -> new MethodStats()).recordSuccess(responseTime);
    }
    
    /**
     * 记录请求失败
     */
    public void recordRequestError(String serviceName, String methodName, String error) {
        totalErrors.increment();
        
        String serviceKey = serviceName;
        String methodKey = serviceName + "." + methodName;
        
        serviceStatsMap.computeIfAbsent(serviceKey, k -> new ServiceStats()).recordError(error);
        methodStatsMap.computeIfAbsent(methodKey, k -> new MethodStats()).recordError(error);
    }
    
    /**
     * 记录请求超时
     */
    public void recordRequestTimeout(String serviceName, String methodName) {
        totalTimeouts.increment();
        
        String serviceKey = serviceName;
        String methodKey = serviceName + "." + methodName;
        
        serviceStatsMap.computeIfAbsent(serviceKey, k -> new ServiceStats()).recordTimeout();
        methodStatsMap.computeIfAbsent(methodKey, k -> new MethodStats()).recordTimeout();
    }
    
    /**
     * 更新响应时间统计
     */
    private void updateResponseTimeStats(long responseTime) {
        totalResponseTime.add(responseTime);
        
        long currentMin = minResponseTime.get();
        while (responseTime < currentMin && 
               !minResponseTime.compareAndSet(currentMin, responseTime)) {
            currentMin = minResponseTime.get();
        }
        
        long currentMax = maxResponseTime.get();
        while (responseTime > currentMax && 
               !maxResponseTime.compareAndSet(currentMax, responseTime)) {
            currentMax = maxResponseTime.get();
        }
    }
    
    /**
     * 获取总体统计信息
     */
    public OverallStats getOverallStats() {
        long totalReq = totalRequests.sum();
        long totalResp = totalResponses.sum();
        long totalErr = totalErrors.sum();
        long totalTimeout = totalTimeouts.sum();
        long totalRespTime = totalResponseTime.sum();
        
        double avgResponseTime = totalResp > 0 ? (double) totalRespTime / totalResp : 0.0;
        double errorRate = totalReq > 0 ? (double) totalErr / totalReq : 0.0;
        double timeoutRate = totalReq > 0 ? (double) totalTimeout / totalReq : 0.0;
        
        return new OverallStats(
            totalReq, totalResp, totalErr, totalTimeout,
            minResponseTime.get(), maxResponseTime.get(), avgResponseTime,
            errorRate, timeoutRate
        );
    }
    
    /**
     * 获取服务统计信息
     */
    public ServiceStats getServiceStats(String serviceName) {
        return serviceStatsMap.get(serviceName);
    }
    
    /**
     * 获取方法统计信息
     */
    public MethodStats getMethodStats(String serviceName, String methodName) {
        return methodStatsMap.get(serviceName + "." + methodName);
    }
    
    /**
     * 获取连接池统计信息
     */
    public ConnectionPool.ConnectionPoolStats getConnectionPoolStats() {
        return connectionPool != null ? connectionPool.getStats() : null;
    }
    
    /**
     * 健康检查
     */
    public HealthStatus performHealthCheck() {
        OverallStats stats = getOverallStats();
        ConnectionPool.ConnectionPoolStats poolStats = getConnectionPoolStats();
        
        boolean healthy = true;
        StringBuilder details = new StringBuilder();
        
        // 检查错误率
        if (stats.getErrorRate() > 0.1) { // 错误率超过10%
            healthy = false;
            details.append("错误率过高: ").append(String.format("%.2f%%", stats.getErrorRate() * 100)).append("; ");
        }
        
        // 检查超时率
        if (stats.getTimeoutRate() > 0.05) { // 超时率超过5%
            healthy = false;
            details.append("超时率过高: ").append(String.format("%.2f%%", stats.getTimeoutRate() * 100)).append("; ");
        }
        
        // 检查平均响应时间
        if (stats.getAvgResponseTime() > 1000) { // 平均响应时间超过1秒
            healthy = false;
            details.append("响应时间过长: ").append(String.format("%.2fms", stats.getAvgResponseTime())).append("; ");
        }
        
        // 检查连接池状态
        if (poolStats != null && poolStats.getUtilizationRate() > 0.9) { // 连接池利用率超过90%
            healthy = false;
            details.append("连接池利用率过高: ").append(String.format("%.2f%%", poolStats.getUtilizationRate() * 100)).append("; ");
        }
        
        return new HealthStatus(healthy, details.toString());
    }
    
    /**
     * 启动监控
     */
    private void startMonitoring() {
        // 定期输出统计信息
        monitorExecutor.scheduleWithFixedDelay(() -> {
            try {
                logMonitoringInfo();
            } catch (Exception e) {
                log.error("输出监控信息失败", e);
            }
        }, 60, 60, TimeUnit.SECONDS);
        
        // 定期健康检查
        monitorExecutor.scheduleWithFixedDelay(() -> {
            try {
                HealthStatus health = performHealthCheck();
                if (!health.isHealthy()) {
                    log.warn("健康检查失败: {}", health.getDetails());
                }
            } catch (Exception e) {
                log.error("健康检查失败", e);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    /**
     * 输出监控信息
     */
    private void logMonitoringInfo() {
        OverallStats stats = getOverallStats();
        ConnectionPool.ConnectionPoolStats poolStats = getConnectionPoolStats();
        
        log.info("=== RPC监控统计 ===");
        log.info("总请求数: {}, 成功: {}, 失败: {}, 超时: {}", 
            stats.getTotalRequests(), stats.getTotalResponses(), 
            stats.getTotalErrors(), stats.getTotalTimeouts());
        log.info("响应时间 - 最小: {}ms, 最大: {}ms, 平均: {:.2f}ms", 
            stats.getMinResponseTime(), stats.getMaxResponseTime(), stats.getAvgResponseTime());
        log.info("错误率: {:.2f}%, 超时率: {:.2f}%", 
            stats.getErrorRate() * 100, stats.getTimeoutRate() * 100);
        
        if (poolStats != null) {
            log.info("连接池 - 总数: {}, 活跃: {}, 可用: {}, 利用率: {:.2f}%", 
                poolStats.getTotalConnections(), poolStats.getActiveConnections(),
                poolStats.getAvailableConnections(), poolStats.getUtilizationRate() * 100);
        }
    }
    
    /**
     * 关闭监控器
     */
    public void shutdown() {
        monitorExecutor.shutdown();
        log.info("RPC监控器已关闭");
    }
    
    /**
     * 总体统计信息
     */
    public static class OverallStats {
        private final long totalRequests;
        private final long totalResponses;
        private final long totalErrors;
        private final long totalTimeouts;
        private final long minResponseTime;
        private final long maxResponseTime;
        private final double avgResponseTime;
        private final double errorRate;
        private final double timeoutRate;
        
        public OverallStats(long totalRequests, long totalResponses, long totalErrors, 
                          long totalTimeouts, long minResponseTime, long maxResponseTime, 
                          double avgResponseTime, double errorRate, double timeoutRate) {
            this.totalRequests = totalRequests;
            this.totalResponses = totalResponses;
            this.totalErrors = totalErrors;
            this.totalTimeouts = totalTimeouts;
            this.minResponseTime = minResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.avgResponseTime = avgResponseTime;
            this.errorRate = errorRate;
            this.timeoutRate = timeoutRate;
        }
        
        // getters
        public long getTotalRequests() { return totalRequests; }
        public long getTotalResponses() { return totalResponses; }
        public long getTotalErrors() { return totalErrors; }
        public long getTotalTimeouts() { return totalTimeouts; }
        public long getMinResponseTime() { return minResponseTime; }
        public long getMaxResponseTime() { return maxResponseTime; }
        public double getAvgResponseTime() { return avgResponseTime; }
        public double getErrorRate() { return errorRate; }
        public double getTimeoutRate() { return timeoutRate; }
    }
    
    /**
     * 服务统计信息
     */
    public static class ServiceStats {
        private final LongAdder requests = new LongAdder();
        private final LongAdder responses = new LongAdder();
        private final LongAdder errors = new LongAdder();
        private final LongAdder timeouts = new LongAdder();
        private final LongAdder totalTime = new LongAdder();
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxTime = new AtomicLong(0);
        
        public void recordRequest() { requests.increment(); }
        public void recordSuccess(long responseTime) { 
            responses.increment(); 
            totalTime.add(responseTime);
            updateTimeStats(responseTime);
        }
        public void recordError(String error) { errors.increment(); }
        public void recordTimeout() { timeouts.increment(); }
        
        private void updateTimeStats(long responseTime) {
            long currentMin = minTime.get();
            while (responseTime < currentMin && !minTime.compareAndSet(currentMin, responseTime)) {
                currentMin = minTime.get();
            }
            
            long currentMax = maxTime.get();
            while (responseTime > currentMax && !maxTime.compareAndSet(currentMax, responseTime)) {
                currentMax = maxTime.get();
            }
        }
        
        public long getRequests() { return requests.sum(); }
        public long getResponses() { return responses.sum(); }
        public long getErrors() { return errors.sum(); }
        public long getTimeouts() { return timeouts.sum(); }
        public double getAvgResponseTime() { 
            long resp = responses.sum(); 
            return resp > 0 ? (double) totalTime.sum() / resp : 0.0; 
        }
        public long getMinResponseTime() { return minTime.get(); }
        public long getMaxResponseTime() { return maxTime.get(); }
    }
    
    /**
     * 方法统计信息
     */
    public static class MethodStats extends ServiceStats {
        // 继承ServiceStats的所有功能
    }
    
    /**
     * 健康状态
     */
    public static class HealthStatus {
        private final boolean healthy;
        private final String details;
        
        public HealthStatus(boolean healthy, String details) {
            this.healthy = healthy;
            this.details = details;
        }
        
        public boolean isHealthy() { return healthy; }
        public String getDetails() { return details; }
    }
} 