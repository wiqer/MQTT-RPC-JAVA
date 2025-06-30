package com.wiqer.rpc.impl.bootstrap;

import com.wiqer.rpc.impl.ioc.AnnotationScanner;
import com.wiqer.rpc.impl.ioc.IOCContainer;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务端启动引导类
 * 参考.NET版本的ConsumerBootstrap
 */
@Slf4j
public class ConsumerBootstrap<T> {
    
    private final AnnotationScanner annotationScanner;
    private final IOCContainer iocContainer;
    private String basePackage;
    
    public ConsumerBootstrap() {
        this.annotationScanner = new AnnotationScanner();
        this.iocContainer = IOCContainer.getInstance();
    }
    
    /**
     * 设置扫描包
     */
    public ConsumerBootstrap<T> scanPackage(String basePackage) {
        this.basePackage = basePackage;
        return this;
    }
    
    /**
     * 启动服务端
     */
    public ConsumerBootstrap<T> start() {
        log.info("启动RPC服务端...");
        
        try {
            // 扫描包并注册服务
            if (basePackage != null && !basePackage.isEmpty()) {
                annotationScanner.scanPackage(basePackage);
            }
            
            log.info("RPC服务端启动成功");
            
        } catch (Exception e) {
            log.error("RPC服务端启动失败", e);
            throw new RuntimeException("RPC服务端启动失败", e);
        }
        
        return this;
    }
    
    /**
     * 启动服务端（带配置）
     */
    public ConsumerBootstrap<T> start(ConsumerOptions options) {
        log.info("启动RPC服务端，配置: {}", options);
        
        try {
            // 设置扫描包
            if (options.getBasePackage() != null) {
                this.basePackage = options.getBasePackage();
            }
            
            // 启动服务
            start();
            
            // 启动消息处理器
            if (options.getMsgHandler() != null) {
                try {
                    // 通过反射调用start方法
                    options.getMsgHandler().getClass().getMethod("start").invoke(options.getMsgHandler());
                } catch (Exception e) {
                    log.warn("启动消息处理器失败", e);
                }
            }
            
        } catch (Exception e) {
            log.error("RPC服务端启动失败", e);
            throw new RuntimeException("RPC服务端启动失败", e);
        }
        
        return this;
    }
    
    /**
     * 获取IOC容器
     */
    public IOCContainer getIocContainer() {
        return iocContainer;
    }
    
    /**
     * 获取指定类型的Bean
     */
    @SuppressWarnings("unchecked")
    public <R> R getBean(Class<R> beanClass) {
        return iocContainer.getBean(beanClass);
    }
    
    /**
     * 停止服务端
     */
    public void stop() {
        log.info("停止RPC服务端...");
        
        try {
            // 清理缓存
            iocContainer.clearCache();
            
            log.info("RPC服务端已停止");
            
        } catch (Exception e) {
            log.error("停止RPC服务端失败", e);
        }
    }
    
    /**
     * 服务端配置类
     */
    public static class ConsumerOptions {
        private String basePackage;
        private Object msgHandler;
        
        public ConsumerOptions() {}
        
        public ConsumerOptions(String basePackage) {
            this.basePackage = basePackage;
        }
        
        public String getBasePackage() {
            return basePackage;
        }
        
        public ConsumerOptions setBasePackage(String basePackage) {
            this.basePackage = basePackage;
            return this;
        }
        
        public Object getMsgHandler() {
            return msgHandler;
        }
        
        public ConsumerOptions setMsgHandler(Object msgHandler) {
            this.msgHandler = msgHandler;
            return this;
        }
        
        @Override
        public String toString() {
            return "ConsumerOptions{" +
                    "basePackage='" + basePackage + '\'' +
                    ", msgHandler=" + msgHandler +
                    '}';
        }
    }
} 