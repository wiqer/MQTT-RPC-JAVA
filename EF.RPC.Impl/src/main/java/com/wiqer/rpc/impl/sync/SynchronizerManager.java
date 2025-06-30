package com.wiqer.rpc.impl.sync;

import com.wiqer.rpc.serialize.SuperMsgMulti;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 同步器管理器 - 管理所有RPC请求的同步器
 * 参考.NET版本的同步器管理机制
 */
@Slf4j
@Component
public class SynchronizerManager {
    
    // 请求ID与同步器的映射
    private final Map<String, UnsafeSynchronizer> synchronizerMap = new ConcurrentHashMap<>();
    
    // 请求ID与响应结果的映射
    private final Map<String, SuperMsgMulti> responseMap = new ConcurrentHashMap<>();
    
    /**
     * 创建同步器
     */
    public UnsafeSynchronizer createSynchronizer(String requestId, int timeout) {
        UnsafeSynchronizer synchronizer = new UnsafeSynchronizer();
        synchronizer.setSleepTime(timeout);
        synchronizerMap.put(requestId, synchronizer);
        log.debug("创建同步器: {}", requestId);
        return synchronizer;
    }
    
    /**
     * 获取同步器
     */
    public UnsafeSynchronizer getSynchronizer(String requestId) {
        return synchronizerMap.get(requestId);
    }
    
    /**
     * 释放同步器
     */
    public boolean releaseSynchronizer(String requestId) {
        UnsafeSynchronizer synchronizer = synchronizerMap.remove(requestId);
        if (synchronizer != null) {
            boolean released = synchronizer.release();
            log.debug("释放同步器: {} (结果: {})", requestId, released);
            return released;
        }
        return false;
    }
    
    /**
     * 移除同步器
     */
    public void removeSynchronizer(String requestId) {
        synchronizerMap.remove(requestId);
        responseMap.remove(requestId);
        log.debug("移除同步器: {}", requestId);
    }
    
    /**
     * 设置响应结果
     */
    public void setResponse(String requestId, SuperMsgMulti response) {
        responseMap.put(requestId, response);
        log.debug("设置响应结果: {}", requestId);
    }
    
    /**
     * 获取响应结果
     */
    public SuperMsgMulti getResponse(String requestId) {
        return responseMap.get(requestId);
    }
    
    /**
     * 清理超时的同步器
     */
    public void cleanupTimeoutSynchronizers() {
        synchronizerMap.entrySet().removeIf(entry -> {
            UnsafeSynchronizer synchronizer = entry.getValue();
            if (synchronizer.isTimeout()) {
                log.warn("清理超时同步器: {}", entry.getKey());
                responseMap.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * 获取同步器数量
     */
    public int getSynchronizerCount() {
        return synchronizerMap.size();
    }
    
    /**
     * 不安全同步器实现
     */
    public static class UnsafeSynchronizer extends Synchronizer {
        
        private volatile boolean timeout = false;
        private final long startTime = System.currentTimeMillis();
        private final int timeoutMs = 30000; // 默认30秒超时
        
        @Override
        public boolean tryAcquire() {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                timeout = true;
                return false;
            }
            return super.tryAcquire();
        }
        
        public boolean isTimeout() {
            return timeout || (System.currentTimeMillis() - startTime > timeoutMs);
        }
    }
} 