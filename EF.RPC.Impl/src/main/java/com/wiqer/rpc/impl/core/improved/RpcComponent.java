package com.wiqer.rpc.impl.core.improved;

/**
 * RPC组件基础接口
 * 改进设计：清晰的职责分离，每个组件只负责一个功能
 */
public interface RpcComponent {
    
    /**
     * 获取组件名称
     */
    String getComponentName();
    
    /**
     * 获取组件版本
     */
    String getComponentVersion();
    
    /**
     * 初始化组件
     */
    void initialize();
    
    /**
     * 启动组件
     */
    void start();
    
    /**
     * 停止组件
     */
    void stop();
    
    /**
     * 检查组件是否运行中
     */
    boolean isRunning();
    
    /**
     * 获取组件状态
     */
    ComponentStatus getStatus();
    
    /**
     * 组件状态枚举
     */
    enum ComponentStatus {
        INITIALIZED,    // 已初始化
        STARTING,       // 启动中
        RUNNING,        // 运行中
        STOPPING,       // 停止中
        STOPPED,        // 已停止
        ERROR           // 错误状态
    }
} 