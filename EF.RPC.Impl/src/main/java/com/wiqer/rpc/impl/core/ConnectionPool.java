package com.wiqer.rpc.impl.core;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 连接池管理器 - 提供连接复用和性能优化
 * 参考.NET版本的连接管理机制
 */
@Slf4j
public class ConnectionPool {
    
    private final BlockingQueue<PooledConnection> availableConnections;
    private final ConcurrentHashMap<String, PooledConnection> activeConnections;
    private final AtomicInteger totalConnections;
    private final AtomicInteger activeCount;
    private final AtomicLong totalWaitTime;
    private final AtomicLong totalWaitCount;
    
    private final int maxConnections;
    private final int minConnections;
    private final long connectionTimeout;
    private final long maxIdleTime;
    private final ConnectionFactory connectionFactory;
    
    public ConnectionPool(ConnectionFactory connectionFactory, int maxConnections, int minConnections) {
        this.connectionFactory = connectionFactory;
        this.maxConnections = maxConnections;
        this.minConnections = minConnections;
        this.connectionTimeout = 30000; // 30秒
        this.maxIdleTime = 300000; // 5分钟
        
        this.availableConnections = new LinkedBlockingQueue<>(maxConnections);
        this.activeConnections = new ConcurrentHashMap<>();
        this.totalConnections = new AtomicInteger(0);
        this.activeCount = new AtomicInteger(0);
        this.totalWaitTime = new AtomicLong(0);
        this.totalWaitCount = new AtomicLong(0);
        
        // 初始化最小连接数
        initializeMinConnections();
        
        // 启动清理线程
        startCleanupThread();
    }
    
    /**
     * 获取连接
     */
    public PooledConnection getConnection(String key) throws InterruptedException, TimeoutException {
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < connectionTimeout) {
            // 尝试从可用连接池获取
            PooledConnection connection = availableConnections.poll();
            if (connection != null && connection.isValid()) {
                activeConnections.put(key, connection);
                activeCount.incrementAndGet();
                log.debug("复用连接: {}", key);
                return connection;
            }
            
            // 尝试创建新连接
            if (totalConnections.get() < maxConnections) {
                connection = createNewConnection(key);
                if (connection != null) {
                    activeConnections.put(key, connection);
                    activeCount.incrementAndGet();
                    log.debug("创建新连接: {}", key);
                    return connection;
                }
            }
            
            // 等待可用连接
            Thread.sleep(10);
        }
        
        // 记录等待统计
        long waitTime = System.currentTimeMillis() - startTime;
        totalWaitTime.addAndGet(waitTime);
        totalWaitCount.incrementAndGet();
        
        throw new TimeoutException("获取连接超时");
    }
    
    /**
     * 释放连接
     */
    public void releaseConnection(String key) {
        PooledConnection connection = activeConnections.remove(key);
        if (connection != null) {
            activeCount.decrementAndGet();
            
            if (connection.isValid() && availableConnections.size() < maxConnections) {
                connection.setLastUsedTime(System.currentTimeMillis());
                availableConnections.offer(connection);
                log.debug("归还连接到池: {}", key);
            } else {
                closeConnection(connection);
                totalConnections.decrementAndGet();
                log.debug("关闭连接: {}", key);
            }
        }
    }
    
    /**
     * 创建新连接
     */
    private PooledConnection createNewConnection(String key) {
        try {
            Object connection = connectionFactory.createConnection();
            PooledConnection pooledConnection = new PooledConnection(connection, key);
            totalConnections.incrementAndGet();
            return pooledConnection;
        } catch (Exception e) {
            log.error("创建连接失败: {}", key, e);
            return null;
        }
    }
    
    /**
     * 关闭连接
     */
    private void closeConnection(PooledConnection connection) {
        try {
            if (connection.getConnection() instanceof Closeable) {
                ((Closeable) connection.getConnection()).close();
            }
        } catch (IOException e) {
            log.error("关闭连接失败", e);
        }
    }
    
    /**
     * 初始化最小连接数
     */
    private void initializeMinConnections() {
        for (int i = 0; i < minConnections; i++) {
            PooledConnection connection = createNewConnection("init-" + i);
            if (connection != null) {
                availableConnections.offer(connection);
            }
        }
    }
    
    /**
     * 启动清理线程
     */
    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    cleanupIdleConnections();
                    Thread.sleep(60000); // 每分钟清理一次
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.setName("ConnectionPool-Cleanup");
        cleanupThread.start();
    }
    
    /**
     * 清理空闲连接
     */
    private void cleanupIdleConnections() {
        long currentTime = System.currentTimeMillis();
        int removedCount = 0;
        
        while (availableConnections.size() > minConnections) {
            PooledConnection connection = availableConnections.poll();
            if (connection != null && connection.isIdle(currentTime, maxIdleTime)) {
                closeConnection(connection);
                totalConnections.decrementAndGet();
                removedCount++;
            } else if (connection != null) {
                availableConnections.offer(connection);
                break;
            }
        }
        
        if (removedCount > 0) {
            log.info("清理了 {} 个空闲连接", removedCount);
        }
    }
    
    /**
     * 获取连接池统计信息
     */
    public ConnectionPoolStats getStats() {
        return new ConnectionPoolStats(
            totalConnections.get(),
            activeCount.get(),
            availableConnections.size(),
            totalWaitTime.get(),
            totalWaitCount.get()
        );
    }
    
    /**
     * 连接工厂接口
     */
    public interface ConnectionFactory {
        Object createConnection() throws Exception;
    }
    
    /**
     * 池化连接
     */
    public static class PooledConnection {
        private final Object connection;
        private final String key;
        private final long createTime;
        private long lastUsedTime;
        private boolean valid = true;
        
        public PooledConnection(Object connection, String key) {
            this.connection = connection;
            this.key = key;
            this.createTime = System.currentTimeMillis();
            this.lastUsedTime = this.createTime;
        }
        
        public Object getConnection() {
            return connection;
        }
        
        public String getKey() {
            return key;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
        public long getLastUsedTime() {
            return lastUsedTime;
        }
        
        public void setLastUsedTime(long lastUsedTime) {
            this.lastUsedTime = lastUsedTime;
        }
        
        public boolean isIdle(long currentTime, long maxIdleTime) {
            return currentTime - lastUsedTime > maxIdleTime;
        }
    }
    
    /**
     * 连接池统计信息
     */
    public static class ConnectionPoolStats {
        private final int totalConnections;
        private final int activeConnections;
        private final int availableConnections;
        private final long totalWaitTime;
        private final long totalWaitCount;
        
        public ConnectionPoolStats(int totalConnections, int activeConnections, 
                                 int availableConnections, long totalWaitTime, long totalWaitCount) {
            this.totalConnections = totalConnections;
            this.activeConnections = activeConnections;
            this.availableConnections = availableConnections;
            this.totalWaitTime = totalWaitTime;
            this.totalWaitCount = totalWaitCount;
        }
        
        public int getTotalConnections() { return totalConnections; }
        public int getActiveConnections() { return activeConnections; }
        public int getAvailableConnections() { return availableConnections; }
        public double getUtilizationRate() { 
            return totalConnections > 0 ? (double) activeConnections / totalConnections : 0.0; 
        }
        public double getAverageWaitTime() { 
            return totalWaitCount > 0 ? (double) totalWaitTime / totalWaitCount : 0.0; 
        }
    }
} 