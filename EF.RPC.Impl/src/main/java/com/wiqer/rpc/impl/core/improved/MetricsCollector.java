package com.wiqer.rpc.impl.core.improved;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 监控指标收集器
 * 改进设计：收集关键性能指标，支持监控和治理
 */
public class MetricsCollector {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);
    
    // 请求计数器
    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder successfulRequests = new LongAdder();
    private final LongAdder failedRequests = new LongAdder();
    private final LongAdder timeoutRequests = new LongAdder();
    
    // 响应时间统计
    private final ConcurrentHashMap<String, ResponseTimeStats> responseTimeStats = new ConcurrentHashMap<>();
    
    // 错误统计
    private final ConcurrentHashMap<String, LongAdder> errorCounters = new ConcurrentHashMap<>();
    
    // 吞吐量统计
    private final AtomicLong lastResetTime = new AtomicLong(System.currentTimeMillis());
    private final LongAdder requestsPerSecond = new LongAdder();
    
    // 连接统计
    private final AtomicLong totalConnections = new AtomicLong(0);
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong connectionErrors = new AtomicLong(0);
    
    // 序列化统计
    private final LongAdder serializationCount = new LongAdder();
    private final LongAdder deserializationCount = new LongAdder();
    private final LongAdder serializationErrors = new LongAdder();
    private final LongAdder deserializationErrors = new LongAdder();
    
    /**
     * 记录请求
     */
    public void recordRequest(String serviceName, String methodName) {
        totalRequests.increment();
        requestsPerSecond.increment();
        
        String key = serviceName + "." + methodName;
        responseTimeStats.computeIfAbsent(key, k -> new ResponseTimeStats());
        
        logger.debug("Recorded request: {}.{}", serviceName, methodName);
    }
    
    /**
     * 记录响应
     */
    public void recordResponse(String serviceName, String methodName, long duration, boolean success) {
        String key = serviceName + "." + methodName;
        ResponseTimeStats stats = responseTimeStats.get(key);
        if (stats != null) {
            stats.recordResponse(duration);
        }
        
        if (success) {
            successfulRequests.increment();
        } else {
            failedRequests.increment();
        }
        
        logger.debug("Recorded response: {}.{} in {}ms, success={}", 
                serviceName, methodName, duration, success);
    }
    
    /**
     * 记录超时
     */
    public void recordTimeout(String serviceName, String methodName) {
        timeoutRequests.increment();
        failedRequests.increment();
        
        String key = serviceName + "." + methodName;
        ResponseTimeStats stats = responseTimeStats.get(key);
        if (stats != null) {
            stats.recordTimeout();
        }
        
        logger.debug("Recorded timeout: {}.{}", serviceName, methodName);
    }
    
    /**
     * 记录错误
     */
    public void recordError(String serviceName, String methodName, String errorType) {
        String key = serviceName + "." + methodName + "." + errorType;
        errorCounters.computeIfAbsent(key, k -> new LongAdder()).increment();
        failedRequests.increment();
        
        logger.debug("Recorded error: {}.{} - {}", serviceName, methodName, errorType);
    }
    
    /**
     * 记录连接事件
     */
    public void recordConnectionCreated() {
        totalConnections.incrementAndGet();
        activeConnections.incrementAndGet();
    }
    
    public void recordConnectionClosed() {
        activeConnections.decrementAndGet();
    }
    
    public void recordConnectionError() {
        connectionErrors.incrementAndGet();
    }
    
    /**
     * 记录序列化事件
     */
    public void recordSerialization(boolean success) {
        if (success) {
            serializationCount.increment();
        } else {
            serializationErrors.increment();
        }
    }
    
    public void recordDeserialization(boolean success) {
        if (success) {
            deserializationCount.increment();
        } else {
            deserializationErrors.increment();
        }
    }
    
    /**
     * 获取指标快照
     */
    public MetricsSnapshot getSnapshot() {
        long currentTime = System.currentTimeMillis();
        long duration = currentTime - lastResetTime.get();
        
        return new MetricsSnapshot(
            totalRequests.sum(),
            successfulRequests.sum(),
            failedRequests.sum(),
            timeoutRequests.sum(),
            duration > 0 ? (double) requestsPerSecond.sum() / (duration / 1000.0) : 0.0,
            totalConnections.get(),
            activeConnections.get(),
            connectionErrors.get(),
            serializationCount.sum(),
            deserializationCount.sum(),
            serializationErrors.sum(),
            deserializationErrors.sum(),
            currentTime,
            responseTimeStats,
            errorCounters
        );
    }
    
    /**
     * 重置指标
     */
    public void reset() {
        totalRequests.reset();
        successfulRequests.reset();
        failedRequests.increment();
        timeoutRequests.reset();
        requestsPerSecond.reset();
        responseTimeStats.clear();
        errorCounters.clear();
        lastResetTime.set(System.currentTimeMillis());
        
        logger.info("Metrics reset");
    }
    
    /**
     * 响应时间统计
     */
    public static class ResponseTimeStats {
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicLong totalTime = new AtomicLong(0);
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxTime = new AtomicLong(0);
        private final AtomicLong timeoutCount = new AtomicLong(0);
        
        public void recordResponse(long duration) {
            count.incrementAndGet();
            totalTime.addAndGet(duration);
            
            // 更新最小时间
            long currentMin = minTime.get();
            while (duration < currentMin && !minTime.compareAndSet(currentMin, duration)) {
                currentMin = minTime.get();
            }
            
            // 更新最大时间
            long currentMax = maxTime.get();
            while (duration > currentMax && !maxTime.compareAndSet(currentMax, duration)) {
                currentMax = maxTime.get();
            }
        }
        
        public void recordTimeout() {
            timeoutCount.incrementAndGet();
        }
        
        public long getCount() { return count.get(); }
        public long getTotalTime() { return totalTime.get(); }
        public long getMinTime() { return minTime.get() == Long.MAX_VALUE ? 0 : minTime.get(); }
        public long getMaxTime() { return maxTime.get(); }
        public long getTimeoutCount() { return timeoutCount.get(); }
        
        public double getAverageTime() {
            long count = this.count.get();
            return count > 0 ? (double) totalTime.get() / count : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("ResponseTimeStats{count=%d, avg=%.2fms, min=%dms, max=%dms, timeouts=%d}",
                    getCount(), getAverageTime(), getMinTime(), getMaxTime(), getTimeoutCount());
        }
    }
    
    /**
     * 指标快照
     */
    public static class MetricsSnapshot {
        private final long totalRequests;
        private final long successfulRequests;
        private final long failedRequests;
        private final long timeoutRequests;
        private final double requestsPerSecond;
        private final long totalConnections;
        private final long activeConnections;
        private final long connectionErrors;
        private final long serializationCount;
        private final long deserializationCount;
        private final long serializationErrors;
        private final long deserializationErrors;
        private final long timestamp;
        private final ConcurrentHashMap<String, ResponseTimeStats> responseTimeStats;
        private final ConcurrentHashMap<String, LongAdder> errorCounters;
        
        public MetricsSnapshot(long totalRequests, long successfulRequests, long failedRequests,
                             long timeoutRequests, double requestsPerSecond, long totalConnections,
                             long activeConnections, long connectionErrors, long serializationCount,
                             long deserializationCount, long serializationErrors, long deserializationErrors,
                             long timestamp, ConcurrentHashMap<String, ResponseTimeStats> responseTimeStats,
                             ConcurrentHashMap<String, LongAdder> errorCounters) {
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.timeoutRequests = timeoutRequests;
            this.requestsPerSecond = requestsPerSecond;
            this.totalConnections = totalConnections;
            this.activeConnections = activeConnections;
            this.connectionErrors = connectionErrors;
            this.serializationCount = serializationCount;
            this.deserializationCount = deserializationCount;
            this.serializationErrors = serializationErrors;
            this.deserializationErrors = deserializationErrors;
            this.timestamp = timestamp;
            this.responseTimeStats = responseTimeStats;
            this.errorCounters = errorCounters;
        }
        
        // Getters
        public long getTotalRequests() { return totalRequests; }
        public long getSuccessfulRequests() { return successfulRequests; }
        public long getFailedRequests() { return failedRequests; }
        public long getTimeoutRequests() { return timeoutRequests; }
        public double getRequestsPerSecond() { return requestsPerSecond; }
        public long getTotalConnections() { return totalConnections; }
        public long getActiveConnections() { return activeConnections; }
        public long getConnectionErrors() { return connectionErrors; }
        public long getSerializationCount() { return serializationCount; }
        public long getDeserializationCount() { return deserializationCount; }
        public long getSerializationErrors() { return serializationErrors; }
        public long getDeserializationErrors() { return deserializationErrors; }
        public long getTimestamp() { return timestamp; }
        public ConcurrentHashMap<String, ResponseTimeStats> getResponseTimeStats() { return responseTimeStats; }
        public ConcurrentHashMap<String, LongAdder> getErrorCounters() { return errorCounters; }
        
        public double getSuccessRate() {
            return totalRequests > 0 ? (double) successfulRequests / totalRequests : 0.0;
        }
        
        public double getErrorRate() {
            return totalRequests > 0 ? (double) failedRequests / totalRequests : 0.0;
        }
        
        public double getTimeoutRate() {
            return totalRequests > 0 ? (double) timeoutRequests / totalRequests : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("MetricsSnapshot{total=%d, success=%.2f%%, rps=%.2f, connections=%d/%d, errors=%d}",
                    totalRequests, getSuccessRate() * 100, requestsPerSecond, 
                    activeConnections, totalConnections, failedRequests);
        }
    }
} 