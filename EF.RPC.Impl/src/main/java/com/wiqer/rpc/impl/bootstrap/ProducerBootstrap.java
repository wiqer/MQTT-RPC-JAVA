package com.wiqer.rpc.impl.bootstrap;

import com.wiqer.rpc.impl.ioc.AnnotationScanner;
import com.wiqer.rpc.impl.ioc.IOCContainer;
import com.wiqer.rpc.impl.proxy.DynamicProxyFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端启动引导类
 * 参考.NET版本的ProducerBootstrap
 */
@Slf4j
public class ProducerBootstrap<T> {
    
    private final AnnotationScanner annotationScanner;
    private final IOCContainer iocContainer;
    private final DynamicProxyFactory proxyFactory;
    private String basePackage;
    
    public ProducerBootstrap() {
        this.annotationScanner = new AnnotationScanner();
        this.iocContainer = IOCContainer.getInstance();
        this.proxyFactory = new DynamicProxyFactory();
    }
    
    /**
     * 设置扫描包
     */
    public ProducerBootstrap<T> scanPackage(String basePackage) {
        this.basePackage = basePackage;
        return this;
    }
    
    /**
     * 启动客户端
     */
    public ProducerBootstrap<T> start() {
        log.info("启动RPC客户端...");
        
        try {
            // 扫描包并注入依赖
            if (basePackage != null && !basePackage.isEmpty()) {
                annotationScanner.scanPackage(basePackage);
            }
            
            log.info("RPC客户端启动成功");
            
        } catch (Exception e) {
            log.error("RPC客户端启动失败", e);
            throw new RuntimeException("RPC客户端启动失败", e);
        }
        
        return this;
    }
    
    /**
     * 启动客户端（带配置）
     */
    public ProducerBootstrap<T> start(ProducerOptions options) {
        log.info("启动RPC客户端，配置: {}", options);
        
        try {
            // 设置扫描包
            if (options.getBasePackage() != null) {
                this.basePackage = options.getBasePackage();
            }
            
            // 启动客户端
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
            log.error("RPC客户端启动失败", e);
            throw new RuntimeException("RPC客户端启动失败", e);
        }
        
        return this;
    }
    
    /**
     * 获取指定类型的控制器
     */
    @SuppressWarnings("unchecked")
    public <R> R getController(Class<R> controllerClass) {
        try {
            // 从IOC容器获取Bean
            R controller = iocContainer.getBean(controllerClass);
            if (controller == null) {
                // 如果容器中没有，尝试创建代理
                controller = createProxyController(controllerClass);
            }
            return controller;
        } catch (Exception e) {
            log.error("获取控制器失败: {}", controllerClass.getName(), e);
            throw new RuntimeException("获取控制器失败: " + controllerClass.getName(), e);
        }
    }
    
    /**
     * 创建代理控制器
     */
    @SuppressWarnings("unchecked")
    private <R> R createProxyController(Class<R> controllerClass) {
        // 这里需要根据具体的RPC实现来创建代理
        // 暂时返回null，具体实现由子类完成
        log.warn("未实现代理控制器创建: {}", controllerClass.getName());
        return null;
    }
    
    /**
     * 获取IOC容器
     */
    public IOCContainer getIocContainer() {
        return iocContainer;
    }
    
    /**
     * 获取代理工厂
     */
    public DynamicProxyFactory getProxyFactory() {
        return proxyFactory;
    }
    
    /**
     * 停止客户端
     */
    public void stop() {
        log.info("停止RPC客户端...");
        
        try {
            // 清理缓存
            iocContainer.clearCache();
            proxyFactory.clearCache();
            
            log.info("RPC客户端已停止");
            
        } catch (Exception e) {
            log.error("停止RPC客户端失败", e);
        }
    }
    
    /**
     * 客户端配置类
     */
    public static class ProducerOptions {
        private String basePackage;
        private Object msgHandler;
        
        public ProducerOptions() {}
        
        public ProducerOptions(String basePackage) {
            this.basePackage = basePackage;
        }
        
        public String getBasePackage() {
            return basePackage;
        }
        
        public ProducerOptions setBasePackage(String basePackage) {
            this.basePackage = basePackage;
            return this;
        }
        
        public Object getMsgHandler() {
            return msgHandler;
        }
        
        public ProducerOptions setMsgHandler(Object msgHandler) {
            this.msgHandler = msgHandler;
            return this;
        }
        
        @Override
        public String toString() {
            return "ProducerOptions{" +
                    "basePackage='" + basePackage + '\'' +
                    ", msgHandler=" + msgHandler +
                    '}';
        }
    }
} 