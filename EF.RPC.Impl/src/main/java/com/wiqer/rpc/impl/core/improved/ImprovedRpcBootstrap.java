package com.wiqer.rpc.impl.core.improved;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 改进的RPC启动器
 * 改进设计：统一的启动管理，支持配置验证和监控
 */
public class ImprovedRpcBootstrap {
    
    private static final Logger logger = LoggerFactory.getLogger(ImprovedRpcBootstrap.class);
    
    private final ConcurrentHashMap<String, RpcComponent> components = new ConcurrentHashMap<>();
    private final MetricsCollector metricsCollector = new MetricsCollector();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    
    private RpcConfig config;
    
    public ImprovedRpcBootstrap() {
        this.config = RpcConfig.createDefault();
    }
    
    /**
     * 配置RPC框架
     */
    public ImprovedRpcBootstrap configure(RpcConfig config) {
        if (started.get()) {
            throw new IllegalStateException("Cannot configure after startup");
        }
        
        // 验证配置
        config.validate();
        this.config = config;
        
        logger.info("RPC framework configured: {}", config);
        return this;
    }
    
    /**
     * 注册组件
     */
    public ImprovedRpcBootstrap registerComponent(String name, RpcComponent component) {
        if (started.get()) {
            throw new IllegalStateException("Cannot register component after startup");
        }
        
        components.put(name, component);
        logger.info("Registered component: {} - {}", name, component.getComponentName());
        return this;
    }
    
    /**
     * 启动RPC框架
     */
    public ImprovedRpcBootstrap start() {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("RPC framework already started");
        }
        
        if (shutdown.get()) {
            throw new IllegalStateException("RPC framework is shutdown");
        }
        
        logger.info("Starting RPC framework...");
        
        try {
            // 初始化所有组件
            for (RpcComponent component : components.values()) {
                try {
                    component.initialize();
                    logger.debug("Initialized component: {}", component.getComponentName());
                } catch (Exception e) {
                    logger.error("Failed to initialize component: {}", component.getComponentName(), e);
                    throw new RuntimeException("Component initialization failed", e);
                }
            }
            
            // 启动所有组件
            for (RpcComponent component : components.values()) {
                try {
                    component.start();
                    logger.debug("Started component: {}", component.getComponentName());
                } catch (Exception e) {
                    logger.error("Failed to start component: {}", component.getComponentName(), e);
                    throw new RuntimeException("Component startup failed", e);
                }
            }
            
            logger.info("RPC framework started successfully with {} components", components.size());
            
        } catch (Exception e) {
            // 启动失败，清理资源
            stop();
            throw new RuntimeException("Failed to start RPC framework", e);
        }
        
        return this;
    }
    
    /**
     * 停止RPC框架
     */
    public void stop() {
        if (!shutdown.compareAndSet(false, true)) {
            return; // 已经停止
        }
        
        logger.info("Stopping RPC framework...");
        
        // 停止所有组件（逆序）
        components.values().stream()
                .sorted((c1, c2) -> c2.getComponentName().compareTo(c1.getComponentName()))
                .forEach(component -> {
                    try {
                        if (component.isRunning()) {
                            component.stop();
                            logger.debug("Stopped component: {}", component.getComponentName());
                        }
                    } catch (Exception e) {
                        logger.error("Failed to stop component: {}", component.getComponentName(), e);
                    }
                });
        
        started.set(false);
        logger.info("RPC framework stopped");
    }
    
    /**
     * 获取组件
     */
    @SuppressWarnings("unchecked")
    public <T extends RpcComponent> T getComponent(String name, Class<T> type) {
        RpcComponent component = components.get(name);
        if (component == null) {
            throw new IllegalArgumentException("Component not found: " + name);
        }
        
        if (!type.isInstance(component)) {
            throw new IllegalArgumentException("Component type mismatch: expected " + type.getName() + ", got " + component.getClass().getName());
        }
        
        return (T) component;
    }
    
    /**
     * 获取所有组件
     */
    public ConcurrentHashMap<String, RpcComponent> getAllComponents() {
        return new ConcurrentHashMap<>(components);
    }
    
    /**
     * 获取配置
     */
    public RpcConfig getConfig() {
        return config;
    }
    
    /**
     * 获取监控指标收集器
     */
    public MetricsCollector getMetricsCollector() {
        return metricsCollector;
    }
    
    /**
     * 检查是否已启动
     */
    public boolean isStarted() {
        return started.get();
    }
    
    /**
     * 检查是否已关闭
     */
    public boolean isShutdown() {
        return shutdown.get();
    }
    
    /**
     * 获取框架状态
     */
    public FrameworkStatus getStatus() {
        if (shutdown.get()) {
            return FrameworkStatus.SHUTDOWN;
        }
        
        if (started.get()) {
            // 检查所有组件状态
            boolean allRunning = components.values().stream()
                    .allMatch(RpcComponent::isRunning);
            
            return allRunning ? FrameworkStatus.RUNNING : FrameworkStatus.ERROR;
        }
        
        return FrameworkStatus.STOPPED;
    }
    
    /**
     * 框架状态枚举
     */
    public enum FrameworkStatus {
        STOPPED,    // 已停止
        RUNNING,    // 运行中
        ERROR,      // 错误状态
        SHUTDOWN    // 已关闭
    }
    
    /**
     * 创建默认启动器
     */
    public static ImprovedRpcBootstrap createDefault() {
        return new ImprovedRpcBootstrap();
    }
    
    /**
     * 创建高性能启动器
     */
    public static ImprovedRpcBootstrap createHighPerformance() {
        ImprovedRpcBootstrap bootstrap = new ImprovedRpcBootstrap();
        bootstrap.configure(RpcConfig.createHighPerformance());
        return bootstrap;
    }
    
    /**
     * 创建高可用启动器
     */
    public static ImprovedRpcBootstrap createHighAvailability() {
        ImprovedRpcBootstrap bootstrap = new ImprovedRpcBootstrap();
        bootstrap.configure(RpcConfig.createHighAvailability());
        return bootstrap;
    }
    
    /**
     * 添加关闭钩子
     */
    public ImprovedRpcBootstrap withShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered");
            stop();
        }, "RPC-Shutdown-Hook"));
        
        return this;
    }
    
    @Override
    public String toString() {
        return String.format("ImprovedRpcBootstrap{status=%s, components=%d, config=%s}",
                getStatus(), components.size(), config);
    }
} 