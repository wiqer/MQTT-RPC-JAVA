package com.wiqer.rpc.impl.bootstrap;

import com.wiqer.rpc.impl.ioc.EnhancedIOCContainer;
import com.wiqer.rpc.impl.proxy.EnhancedDynamicProxyFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * 增强的RPC启动器
 * 改进设计：统一的启动和配置管理，遵循开闭原则
 */
@Slf4j
public class EnhancedRpcBootstrap {
    
    private final EnhancedIOCContainer iocContainer;
    private final EnhancedDynamicProxyFactory proxyFactory;
    private final RpcConfiguration configuration;
    
    public EnhancedRpcBootstrap() {
        this.iocContainer = EnhancedIOCContainer.getInstance();
        this.proxyFactory = new EnhancedDynamicProxyFactory();
        this.configuration = new RpcConfiguration();
    }
    
    /**
     * 配置RPC框架
     */
    public EnhancedRpcBootstrap configure(RpcConfiguration config) {
        this.configuration.merge(config);
        log.info("配置RPC框架: {}", config);
        return this;
    }
    
    /**
     * 启动RPC框架
     */
    public EnhancedRpcBootstrap start() {
        log.info("启动增强的RPC框架...");
        
        // 初始化IOC容器
        initializeIOCContainer();
        
        // 初始化代理工厂
        initializeProxyFactory();
        
        // 扫描和注册服务
        scanAndRegisterServices();
        
        log.info("增强的RPC框架启动完成");
        return this;
    }
    
    /**
     * 停止RPC框架
     */
    public void stop() {
        log.info("停止增强的RPC框架...");
        
        // 清除缓存
        iocContainer.clearCache();
        proxyFactory.clearCache();
        
        log.info("增强的RPC框架已停止");
    }
    
    /**
     * 获取控制器
     */
    @SuppressWarnings("unchecked")
    public <T> T getController(Class<T> controllerClass) {
        return (T) iocContainer.getBean(controllerClass);
    }
    
    /**
     * 初始化IOC容器
     */
    private void initializeIOCContainer() {
        log.debug("初始化IOC容器");
        // IOC容器已经在构造函数中初始化
    }
    
    /**
     * 初始化代理工厂
     */
    private void initializeProxyFactory() {
        log.debug("初始化代理工厂");
        // 代理工厂已经在构造函数中初始化
    }
    
    /**
     * 扫描和注册服务
     */
    private void scanAndRegisterServices() {
        log.debug("扫描和注册服务");
        // TODO: 实现服务扫描和注册逻辑
    }
    
    /**
     * 获取IOC容器
     */
    public EnhancedIOCContainer getIocContainer() {
        return iocContainer;
    }
    
    /**
     * 获取代理工厂
     */
    public EnhancedDynamicProxyFactory getProxyFactory() {
        return proxyFactory;
    }
    
    /**
     * 获取配置
     */
    public RpcConfiguration getConfiguration() {
        return configuration;
    }
    
    /**
     * RPC配置类
     */
    public static class RpcConfiguration {
        private String version = "v1";
        private String runMode = "auto";
        private long timeout = 5000;
        private String scanPackage = "";
        private boolean enableCache = true;
        private boolean enableLogging = true;
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getRunMode() { return runMode; }
        public void setRunMode(String runMode) { this.runMode = runMode; }
        
        public long getTimeout() { return timeout; }
        public void setTimeout(long timeout) { this.timeout = timeout; }
        
        public String getScanPackage() { return scanPackage; }
        public void setScanPackage(String scanPackage) { this.scanPackage = scanPackage; }
        
        public boolean isEnableCache() { return enableCache; }
        public void setEnableCache(boolean enableCache) { this.enableCache = enableCache; }
        
        public boolean isEnableLogging() { return enableLogging; }
        public void setEnableLogging(boolean enableLogging) { this.enableLogging = enableLogging; }
        
        /**
         * 合并配置
         */
        public void merge(RpcConfiguration other) {
            if (other.version != null) this.version = other.version;
            if (other.runMode != null) this.runMode = other.runMode;
            if (other.timeout > 0) this.timeout = other.timeout;
            if (other.scanPackage != null) this.scanPackage = other.scanPackage;
            this.enableCache = other.enableCache;
            this.enableLogging = other.enableLogging;
        }
        
        @Override
        public String toString() {
            return String.format("RpcConfiguration{version='%s', runMode='%s', timeout=%d, scanPackage='%s', enableCache=%s, enableLogging=%s}", 
                version, runMode, timeout, scanPackage, enableCache, enableLogging);
        }
    }
} 