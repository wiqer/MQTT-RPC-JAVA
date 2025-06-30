package com.wiqer.rpc.test;

import com.wiqer.rpc.impl.sync.Synchronizer;
import com.wiqer.rpc.impl.sync.SynchronizerManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 同步器测试
 */
public class SynchronizerTest {
    
    private SynchronizerManager synchronizerManager;
    
    @BeforeEach
    void setUp() {
        synchronizerManager = new SynchronizerManager();
    }
    
    @Test
    void testSynchronizerCreation() {
        String requestId = "test-request-1";
        SynchronizerManager.UnsafeSynchronizer synchronizer = 
            synchronizerManager.createSynchronizer(requestId, 5000);
        
        assertNotNull(synchronizer);
        assertFalse(synchronizer.isTimeout());
        assertEquals(1, synchronizerManager.getSynchronizerCount());
    }
    
    @Test
    void testSynchronizerRelease() {
        String requestId = "test-request-2";
        SynchronizerManager.UnsafeSynchronizer synchronizer = 
            synchronizerManager.createSynchronizer(requestId, 5000);
        
        boolean released = synchronizerManager.releaseSynchronizer(requestId);
        assertTrue(released);
        assertEquals(0, synchronizerManager.getSynchronizerCount());
    }
    
    @Test
    void testSynchronizerTimeout() throws InterruptedException {
        String requestId = "test-request-3";
        SynchronizerManager.UnsafeSynchronizer synchronizer = 
            synchronizerManager.createSynchronizer(requestId, 100); // 100ms超时
        
        Thread.sleep(200); // 等待超时
        
        assertTrue(synchronizer.isTimeout());
    }
    
    @Test
    void testSynchronizerResponseStorage() {
        String requestId = "test-request-4";
        synchronizerManager.createSynchronizer(requestId, 5000);
        
        // 模拟响应
        Object response = "test-response";
        synchronizerManager.setResponse(requestId, response);
        
        assertEquals(response, synchronizerManager.getResponse(requestId));
    }
    
    @Test
    void testSynchronizerCleanup() {
        String requestId = "test-request-5";
        SynchronizerManager.UnsafeSynchronizer synchronizer = 
            synchronizerManager.createSynchronizer(requestId, 100);
        
        // 模拟超时
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        synchronizerManager.cleanupTimeoutSynchronizers();
        assertEquals(0, synchronizerManager.getSynchronizerCount());
    }
    
    @Test
    void testMultipleSynchronizers() {
        // 创建多个同步器
        for (int i = 0; i < 10; i++) {
            String requestId = "test-request-" + i;
            synchronizerManager.createSynchronizer(requestId, 5000);
        }
        
        assertEquals(10, synchronizerManager.getSynchronizerCount());
        
        // 释放部分同步器
        for (int i = 0; i < 5; i++) {
            String requestId = "test-request-" + i;
            synchronizerManager.releaseSynchronizer(requestId);
        }
        
        assertEquals(5, synchronizerManager.getSynchronizerCount());
    }
    
    @Test
    void testSynchronizerConcurrency() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicBoolean success = new AtomicBoolean(true);
        
        // 创建多个线程同时操作同步器
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            Thread thread = new Thread(() -> {
                try {
                    startLatch.await();
                    
                    String requestId = "concurrent-request-" + index;
                    SynchronizerManager.UnsafeSynchronizer synchronizer = 
                        synchronizerManager.createSynchronizer(requestId, 5000);
                    
                    // 模拟一些操作
                    Thread.sleep(10);
                    
                    boolean released = synchronizerManager.releaseSynchronizer(requestId);
                    if (!released) {
                        success.set(false);
                    }
                    
                } catch (Exception e) {
                    success.set(false);
                } finally {
                    endLatch.countDown();
                }
            });
            thread.start();
        }
        
        startLatch.countDown();
        endLatch.await(5, TimeUnit.SECONDS);
        
        assertTrue(success.get());
        assertEquals(0, synchronizerManager.getSynchronizerCount());
    }
    
    @Test
    void testSynchronizerRemoval() {
        String requestId = "test-request-6";
        synchronizerManager.createSynchronizer(requestId, 5000);
        
        // 设置响应
        Object response = "test-response";
        synchronizerManager.setResponse(requestId, response);
        
        // 移除同步器
        synchronizerManager.removeSynchronizer(requestId);
        
        assertEquals(0, synchronizerManager.getSynchronizerCount());
        assertNull(synchronizerManager.getResponse(requestId));
    }
} 