package com.wiqer.rpc.impl.core.improved;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 连接池管理
 * 改进设计：线程安全的连接池，支持连接复用和监控
 */
public class ConnectionPool {
    
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    
    private final BlockingQueue<Connection> connectionPool;
    private final ConnectionFactory factory;
    private final int maxPoolSize;
    private final int minPoolSize;
    private final long connectionTimeout;
    private final long maxIdleTime;
    
    private final AtomicInteger currentPoolSize = new AtomicInteger(0);
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicLong totalConnectionsCreated = new AtomicLong(0);
    private final AtomicLong totalConnectionsClosed = new AtomicLong(0);
    private final AtomicLong totalWaitTime = new AtomicLong(0);
    private final AtomicLong totalWaitCount = new AtomicLong(0);
    
    private volatile boolean shutdown = false;
    
    /**
     * 连接接口
     */
    public interface Connection {
        /**
         * 检查连接是否有效
         */
        boolean isValid();
        
        /**
         * 关闭连接
         */
        void close();
        
        /**
         * 获取连接创建时间
         */
        long getCreateTime();
        
        /**
         * 获取最后使用时间
         */
        long getLastUsedTime();
        
        /**
         * 更新最后使用时间
         */
        void updateLastUsedTime();
        
        /**
         * 获取连接ID
         */
        String getConnectionId();
    }
    
    /**
     * 连接工厂接口
     */
    public interface ConnectionFactory {
        /**
         * 创建新连接
         */
        Connection createConnection() throws Exception;
        
        /**
         * 验证连接
         */
        boolean validateConnection(Connection connection);
    }
    
    public ConnectionPool(ConnectionFactory factory, RpcConfig config) {
        this.factory = factory;
        this.maxPoolSize = config.getMaxConnections();
        this.minPoolSize = config.getMinConnections();
        this.connectionTimeout = config.getConnectionTimeout();
        this.maxIdleTime = config.getMaxIdleTime();
        
        this.connectionPool = new LinkedBlockingQueue<>(maxPoolSize);
        
        // 初始化连接池
        initializePool();
        
        // 启动清理线程
        startCleanupThread();
        
        logger.info("Connection pool initialized with maxSize={}, minSize={}, timeout={}ms", 
                maxPoolSize, minPoolSize, connectionTimeout);
    }
    
    /**
     * 初始化连接池
     */
    private void initializePool() {
        try {
            for (int i = 0; i < minPoolSize; i++) {
                Connection connection = createNewConnection();
                if (connection != null) {
                    connectionPool.offer(connection);
                }
            }
            logger.info("Initialized {} connections in pool", connectionPool.size());
        } catch (Exception e) {
            logger.error("Failed to initialize connection pool", e);
            throw new RuntimeException("Failed to initialize connection pool", e);
        }
    }
    
    /**
     * 获取连接
     */
    public Connection getConnection() throws InterruptedException {
        if (shutdown) {
            throw new IllegalStateException("Connection pool is shutdown");
        }
        
        long startTime = System.currentTimeMillis();
        
        // 首先尝试从池中获取现有连接
        Connection connection = connectionPool.poll();
        
        if (connection != null) {
            // 验证连接是否有效
            if (factory.validateConnection(connection)) {
                connection.updateLastUsedTime();
                activeConnections.incrementAndGet();
                recordWaitTime(System.currentTimeMillis() - startTime);
                return connection;
            } else {
                // 连接无效，关闭并减少计数
                closeConnection(connection);
                currentPoolSize.decrementAndGet();
            }
        }
        
        // 如果池中没有可用连接，尝试创建新连接
        if (currentPoolSize.get() < maxPoolSize) {
            connection = createNewConnection();
            if (connection != null) {
                activeConnections.incrementAndGet();
                recordWaitTime(System.currentTimeMillis() - startTime);
                return connection;
            }
        }
        
        // 等待可用连接
        connection = connectionPool.poll(connectionTimeout, TimeUnit.MILLISECONDS);
        if (connection != null) {
            if (factory.validateConnection(connection)) {
                connection.updateLastUsedTime();
                activeConnections.incrementAndGet();
                recordWaitTime(System.currentTimeMillis() - startTime);
                return connection;
            } else {
                closeConnection(connection);
                currentPoolSize.decrementAndGet();
            }
        }
        
        // 超时
        recordWaitTime(System.currentTimeMillis() - startTime);
        throw new RuntimeException("Failed to get connection from pool within " + connectionTimeout + "ms");
    }
    
    /**
     * 释放连接
     */
    public void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        
        activeConnections.decrementAndGet();
        
        if (shutdown) {
            closeConnection(connection);
            return;
        }
        
        // 验证连接是否仍然有效
        if (!factory.validateConnection(connection)) {
            closeConnection(connection);
            currentPoolSize.decrementAndGet();
            return;
        }
        
        // 检查连接是否空闲时间过长
        if (System.currentTimeMillis() - connection.getLastUsedTime() > maxIdleTime) {
            closeConnection(connection);
            currentPoolSize.decrementAndGet();
            return;
        }
        
        // 将连接放回池中
        boolean offered = connectionPool.offer(connection);
        if (!offered) {
            // 池已满，关闭连接
            closeConnection(connection);
            currentPoolSize.decrementAndGet();
        }
    }
    
    /**
     * 创建新连接
     */
    private Connection createNewConnection() {
        try {
            Connection connection = factory.createConnection();
            currentPoolSize.incrementAndGet();
            totalConnectionsCreated.incrementAndGet();
            logger.debug("Created new connection: {}", connection.getConnectionId());
            return connection;
        } catch (Exception e) {
            logger.error("Failed to create new connection", e);
            return null;
        }
    }
    
    /**
     * 关闭连接
     */
    private void closeConnection(Connection connection) {
        try {
            connection.close();
            totalConnectionsClosed.incrementAndGet();
            logger.debug("Closed connection: {}", connection.getConnectionId());
        } catch (Exception e) {
            logger.warn("Error closing connection: {}", connection.getConnectionId(), e);
        }
    }
    
    /**
     * 记录等待时间
     */
    private void recordWaitTime(long waitTime) {
        totalWaitTime.addAndGet(waitTime);
        totalWaitCount.incrementAndGet();
    }
    
    /**
     * 启动清理线程
     */
    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (!shutdown) {
                try {
                    Thread.sleep(30000); // 每30秒清理一次
                    cleanupIdleConnections();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error in cleanup thread", e);
                }
            }
        }, "ConnectionPool-Cleanup");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }
    
    /**
     * 清理空闲连接
     */
    private void cleanupIdleConnections() {
        int removedCount = 0;
        long currentTime = System.currentTimeMillis();
        
        // 移除空闲时间过长的连接
        while (currentPoolSize.get() > minPoolSize) {
            Connection connection = connectionPool.poll();
            if (connection == null) {
                break;
            }
            
            if (currentTime - connection.getLastUsedTime() > maxIdleTime) {
                closeConnection(connection);
                currentPoolSize.decrementAndGet();
                removedCount++;
            } else {
                // 放回池中
                connectionPool.offer(connection);
                break;
            }
        }
        
        if (removedCount > 0) {
            logger.info("Cleaned up {} idle connections", removedCount);
        }
    }
    
    /**
     * 关闭连接池
     */
    public void shutdown() {
        shutdown = true;
        
        // 关闭所有连接
        Connection connection;
        while ((connection = connectionPool.poll()) != null) {
            closeConnection(connection);
            currentPoolSize.decrementAndGet();
        }
        
        logger.info("Connection pool shutdown completed");
    }
    
    /**
     * 获取连接池统计信息
     */
    public PoolStats getPoolStats() {
        return new PoolStats(
            currentPoolSize.get(),
            activeConnections.get(),
            connectionPool.size(),
            maxPoolSize,
            minPoolSize,
            totalConnectionsCreated.get(),
            totalConnectionsClosed.get(),
            totalWaitCount.get() > 0 ? totalWaitTime.get() / totalWaitCount.get() : 0
        );
    }
    
    /**
     * 连接池统计信息
     */
    public static class PoolStats {
        private final int currentPoolSize;
        private final int activeConnections;
        private final int idleConnections;
        private final int maxPoolSize;
        private final int minPoolSize;
        private final long totalConnectionsCreated;
        private final long totalConnectionsClosed;
        private final long averageWaitTime;
        
        public PoolStats(int currentPoolSize, int activeConnections, int idleConnections,
                        int maxPoolSize, int minPoolSize, long totalConnectionsCreated,
                        long totalConnectionsClosed, long averageWaitTime) {
            this.currentPoolSize = currentPoolSize;
            this.activeConnections = activeConnections;
            this.idleConnections = idleConnections;
            this.maxPoolSize = maxPoolSize;
            this.minPoolSize = minPoolSize;
            this.totalConnectionsCreated = totalConnectionsCreated;
            this.totalConnectionsClosed = totalConnectionsClosed;
            this.averageWaitTime = averageWaitTime;
        }
        
        // Getters
        public int getCurrentPoolSize() { return currentPoolSize; }
        public int getActiveConnections() { return activeConnections; }
        public int getIdleConnections() { return idleConnections; }
        public int getMaxPoolSize() { return maxPoolSize; }
        public int getMinPoolSize() { return minPoolSize; }
        public long getTotalConnectionsCreated() { return totalConnectionsCreated; }
        public long getTotalConnectionsClosed() { return totalConnectionsClosed; }
        public long getAverageWaitTime() { return averageWaitTime; }
        
        public double getUtilizationRate() {
            return maxPoolSize > 0 ? (double) currentPoolSize / maxPoolSize : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("PoolStats{current=%d, active=%d, idle=%d, max=%d, utilization=%.2f%%, avgWait=%dms}",
                    currentPoolSize, activeConnections, idleConnections, maxPoolSize,
                    getUtilizationRate() * 100, averageWaitTime);
        }
    }
} 