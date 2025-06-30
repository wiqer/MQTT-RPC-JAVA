package com.wiqer.rpc.impl.sync;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 同步器 - 实现RPC调用的线程同步机制
 * 参考.NET版本的Synchronizer实现
 */
@Slf4j
public abstract class Synchronizer {
    
    protected Thread currentThread;
    protected int sleepTime = 0;
    protected final AtomicBoolean released = new AtomicBoolean(false);
    protected final CountDownLatch latch = new CountDownLatch(1);
    
    /**
     * 获取同步器
     */
    public boolean tryAcquire() {
        return !released.get();
    }
    
    /**
     * 释放同步器
     */
    public boolean tryRelease() {
        return released.compareAndSet(false, true);
    }
    
    /**
     * 获取同步器（阻塞）
     */
    public void acquire() {
        if (tryAcquire()) {
            try {
                currentThread = Thread.currentThread();
                
                if (sleepTime > 0) {
                    // 指定超时时间
                    if (!latch.await(sleepTime, TimeUnit.MILLISECONDS)) {
                        log.warn("同步器等待超时: {}ms", sleepTime);
                    }
                } else {
                    // 无限等待
                    latch.await();
                }
            } catch (InterruptedException e) {
                log.debug("同步器被中断唤醒");
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * 释放同步器
     */
    public boolean release() {
        if (tryRelease()) {
            latch.countDown();
            log.debug("同步器已释放");
            return true;
        }
        return false;
    }
    
    /**
     * 设置超时时间
     */
    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }
    
    /**
     * 获取当前线程
     */
    public Thread getCurrentThread() {
        return currentThread;
    }
    
    /**
     * 是否已释放
     */
    public boolean isReleased() {
        return released.get();
    }
} 