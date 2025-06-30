using System;

namespace EF.RPC.Sharing
{
    /// <summary>
    /// RPC组件基础接口
    /// 遵循统一架构设计规范，为所有RPC组件提供标准化的生命周期管理
    /// </summary>
    public interface IRpcComponent
    {
        /// <summary>
        /// 获取组件名称
        /// </summary>
        string ComponentName { get; }
        
        /// <summary>
        /// 获取组件版本
        /// </summary>
        string ComponentVersion { get; }
        
        /// <summary>
        /// 初始化组件
        /// 在启动前调用，用于初始化组件内部状态
        /// </summary>
        void Initialize();
        
        /// <summary>
        /// 启动组件
        /// 启动组件的运行状态
        /// </summary>
        void Start();
        
        /// <summary>
        /// 停止组件
        /// 停止组件的运行状态，释放资源
        /// </summary>
        void Stop();
        
        /// <summary>
        /// 检查组件是否正在运行
        /// </summary>
        bool IsRunning { get; }
        
        /// <summary>
        /// 获取组件当前状态
        /// </summary>
        ComponentStatus Status { get; }
    }
    
    /// <summary>
    /// 组件状态枚举
    /// </summary>
    public enum ComponentStatus
    {
        /// <summary>
        /// 已初始化
        /// </summary>
        Initialized,
        
        /// <summary>
        /// 启动中
        /// </summary>
        Starting,
        
        /// <summary>
        /// 运行中
        /// </summary>
        Running,
        
        /// <summary>
        /// 停止中
        /// </summary>
        Stopping,
        
        /// <summary>
        /// 已停止
        /// </summary>
        Stopped,
        
        /// <summary>
        /// 错误状态
        /// </summary>
        Error
    }
} 