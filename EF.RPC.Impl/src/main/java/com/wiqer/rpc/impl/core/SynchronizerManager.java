package com.wiqer.rpc.impl.core;

import com.wiqer.rpc.serialize.SuperMsgMulti;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 同步器管理器 - 参考.NET版本的Synchronizer实现
 * 提供线程安全的同步机制，支持请求-响应模式
 */
@Slf4j
public class SynchronizerManager {
    
    private final ConcurrentHashMap<String, UnsafeSynchronizer> synchronizerMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SuperMsgMulti> responseMap = new ConcurrentHashMap<>();
    
    /**
     * 创建同步器
     */
    public UnsafeSynchronizer createSynchronizer(String requestId, long timeoutMs) {
        UnsafeSynchronizer synchronizer = new UnsafeSynchronizer(timeoutMs);
        synchronizerMap.put(requestId, synchronizer);
        log.debug("创建同步器: {}", requestId);
        return synchronizer;
    }
    
    /**
     * 释放同步器
     */
    public void releaseSynchronizer(String requestId) {
        UnsafeSynchronizer synchronizer = synchronizerMap.remove(requestId);
        if (synchronizer != null) {
            synchronizer.release();
            log.debug("释放同步器: {}", requestId);
        }
    }
    
    /**
     * 设置响应结果
     */
    public void setResponse(String requestId, SuperMsgMulti response) {
        responseMap.put(requestId, response);
        UnsafeSynchronizer synchronizer = synchronizerMap.get(requestId);
        if (synchronizer != null) {
            synchronizer.release();
        }
        log.debug("设置响应: {}", requestId);
    }
    
    /**
     * 获取响应结果
     */
    public SuperMsgMulti getResponse(String requestId) {
        return responseMap.remove(requestId);
    }
    
    /**
     * 清理过期的同步器
     */
    public void cleanupExpiredSynchronizers() {
        long currentTime = System.currentTimeMillis();
        synchronizerMap.entrySet().removeIf(entry -> {
            UnsafeSynchronizer synchronizer = entry.getValue();
            if (synchronizer.isExpired(currentTime)) {
                log.warn("清理过期同步器: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * 不安全同步器 - 参考.NET版本的Synchronizer
     */
    public static class UnsafeSynchronizer {
        private final long timeoutMs;
        private final long startTime;
        private final AtomicBoolean released = new AtomicBoolean(false);
        private final Object lock = new Object();
        
        public UnsafeSynchronizer(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            this.startTime = System.currentTimeMillis();
        }
        
        /**
         * 获取同步器（阻塞等待）
         */
        public void acquire() throws InterruptedException {
            if (released.get()) {
                return;
            }
            
            synchronized (lock) {
                while (!released.get() && !isExpired()) {
                    lock.wait(timeoutMs);
                }
                
                if (!released.get() && isExpired()) {
                    throw new RuntimeException("同步器超时");
                }
            }
        }
        
        /**
         * 释放同步器
         */
        public boolean release() {
            if (released.compareAndSet(false, true)) {
                synchronized (lock) {
                    lock.notifyAll();
                }
                return true;
            }
            return false;
        }
        
        /**
         * 检查是否过期
         */
        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }
        
        public boolean isExpired(long currentTime) {
            return currentTime - startTime > timeoutMs;
        }
        
        /**
         * 检查是否已释放
         */
        public boolean isReleased() {
            return released.get();
        }
    }
} 