package com.wiqer.rpc.sharing.core;

/**
 * RPC组件基础接口
 * 定义所有RPC组件的基本生命周期管理
 * 
 * @author EFRPC Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface IRpcComponent {
    
    /**
     * 获取组件名称
     * 
     * @return 组件名称
     */
    String getComponentName();
    
    /**
     * 获取组件版本
     * 
     * @return 组件版本号
     */
    String getComponentVersion();
    
    /**
     * 初始化组件
     * 在启动前调用，用于初始化组件内部状态
     * 
     * @throws RpcException 初始化失败时抛出
     */
    void initialize() throws RpcException;
    
    /**
     * 启动组件
     * 
     * @throws RpcException 启动失败时抛出异常
     */
    void start() throws RpcException;
    
    /**
     * 停止组件
     * 
     * @throws RpcException 停止失败时抛出异常
     */
    void stop() throws RpcException;
    
    /**
     * 检查组件是否正在运行
     * 
     * @return true表示正在运行，false表示已停止
     */
    boolean isRunning();
    
    /**
     * 获取组件状态
     * 
     * @return 组件当前状态
     */
    ComponentStatus getStatus();
    
    /**
     * 获取组件配置
     * 
     * @return 组件配置对象
     */
    IRpcConfig getConfig();
    
    /**
     * 组件状态枚举
     */
    enum ComponentStatus {
        /** 已初始化 */
        INITIALIZED,
        /** 启动中 */
        STARTING,
        /** 运行中 */
        RUNNING,
        /** 停止中 */
        STOPPING,
        /** 已停止 */
        STOPPED,
        /** 错误状态 */
        ERROR
    }
} 