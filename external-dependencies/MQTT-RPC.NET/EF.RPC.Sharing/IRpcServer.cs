using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace EF.RPC.Sharing
{
    /// <summary>
    /// RPC服务端接口
    /// 遵循统一架构设计规范，为RPC服务端提供标准化的功能定义
    /// </summary>
    public interface IRpcServer : IRpcComponent
    {
        /// <summary>
        /// 获取服务端名称
        /// </summary>
        string ServerName { get; }
        
        /// <summary>
        /// 获取服务端版本
        /// </summary>
        string ServerVersion { get; }
        
        /// <summary>
        /// 注册服务实现
        /// </summary>
        /// <param name="serviceImpl">服务实现对象</param>
        void RegisterService(object serviceImpl);
        
        /// <summary>
        /// 注册服务实现（带配置）
        /// </summary>
        /// <param name="serviceImpl">服务实现对象</param>
        /// <param name="config">服务配置</param>
        void RegisterService(object serviceImpl, IRpcConfig config);
        
        /// <summary>
        /// 注册服务实现（指定服务名和版本）
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        /// <param name="serviceImpl">服务实现对象</param>
        void RegisterService(string serviceName, string version, object serviceImpl);
        
        /// <summary>
        /// 注册服务实现（指定服务名、版本和配置）
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        /// <param name="serviceImpl">服务实现对象</param>
        /// <param name="config">服务配置</param>
        void RegisterService(string serviceName, string version, object serviceImpl, IRpcConfig config);
        
        /// <summary>
        /// 注销服务
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        void UnregisterService(string serviceName, string version);
        
        /// <summary>
        /// 获取已注册的服务列表
        /// </summary>
        /// <returns>服务列表</returns>
        IList<ServiceInfo> GetRegisteredServices();
        
        /// <summary>
        /// 检查服务是否已注册
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        /// <returns>true表示已注册，false表示未注册</returns>
        bool IsServiceRegistered(string serviceName, string version);
        
        /// <summary>
        /// 获取服务实现
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        /// <returns>服务实现对象</returns>
        object GetService(string serviceName, string version);
        
        /// <summary>
        /// 获取服务配置
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        /// <returns>服务配置</returns>
        IRpcConfig GetServiceConfig(string serviceName, string version);
        
        /// <summary>
        /// 获取服务端统计信息
        /// </summary>
        /// <returns>服务端统计信息</returns>
        ServerStats GetStats();
        
        /// <summary>
        /// 获取服务端配置
        /// </summary>
        /// <returns>服务端配置</returns>
        IRpcConfig GetConfig();
        
        /// <summary>
        /// 设置服务端配置
        /// </summary>
        /// <param name="config">服务端配置</param>
        void SetConfig(IRpcConfig config);
        
        /// <summary>
        /// 获取监听地址
        /// </summary>
        /// <returns>监听地址</returns>
        string GetListenAddress();
        
        /// <summary>
        /// 设置监听地址
        /// </summary>
        /// <param name="address">监听地址</param>
        void SetListenAddress(string address);
        
        /// <summary>
        /// 获取监听端口
        /// </summary>
        /// <returns>监听端口</returns>
        int GetListenPort();
        
        /// <summary>
        /// 设置监听端口
        /// </summary>
        /// <param name="port">监听端口</param>
        void SetListenPort(int port);
        
        /// <summary>
        /// 注册请求处理器
        /// </summary>
        /// <param name="handler">请求处理器</param>
        void RegisterRequestHandler(IRequestHandler handler);
        
        /// <summary>
        /// 注销请求处理器
        /// </summary>
        /// <param name="handler">请求处理器</param>
        void UnregisterRequestHandler(IRequestHandler handler);
        
        /// <summary>
        /// 注册服务生命周期监听器
        /// </summary>
        /// <param name="listener">服务生命周期监听器</param>
        void RegisterServiceLifecycleListener(IServiceLifecycleListener listener);
        
        /// <summary>
        /// 注销服务生命周期监听器
        /// </summary>
        /// <param name="listener">服务生命周期监听器</param>
        void UnregisterServiceLifecycleListener(IServiceLifecycleListener listener);
        
        /// <summary>
        /// 优雅关闭
        /// </summary>
        /// <param name="timeout">超时时间</param>
        /// <returns>关闭任务</returns>
        Task GracefulShutdownAsync(TimeSpan timeout);
    }
    
    /// <summary>
    /// 服务信息
    /// </summary>
    public class ServiceInfo
    {
        /// <summary>
        /// 服务名称
        /// </summary>
        public string ServiceName { get; set; }
        
        /// <summary>
        /// 服务版本
        /// </summary>
        public string Version { get; set; }
        
        /// <summary>
        /// 服务接口类型
        /// </summary>
        public Type ServiceInterface { get; set; }
        
        /// <summary>
        /// 服务实现类型
        /// </summary>
        public Type ServiceImplementation { get; set; }
        
        /// <summary>
        /// 注册时间
        /// </summary>
        public DateTime RegisterTime { get; set; }
        
        /// <summary>
        /// 服务状态
        /// </summary>
        public ServiceStatus Status { get; set; }
        
        /// <summary>
        /// 方法数量
        /// </summary>
        public int MethodCount { get; set; }
    }
    
    /// <summary>
    /// 服务状态枚举
    /// </summary>
    public enum ServiceStatus
    {
        /// <summary>
        /// 已注册
        /// </summary>
        Registered,
        
        /// <summary>
        /// 运行中
        /// </summary>
        Running,
        
        /// <summary>
        /// 已停止
        /// </summary>
        Stopped,
        
        /// <summary>
        /// 错误状态
        /// </summary>
        Error
    }
    
    /// <summary>
    /// 服务端统计信息
    /// </summary>
    public class ServerStats
    {
        /// <summary>
        /// 总请求数
        /// </summary>
        public long TotalRequests { get; set; }
        
        /// <summary>
        /// 成功请求数
        /// </summary>
        public long SuccessfulRequests { get; set; }
        
        /// <summary>
        /// 失败请求数
        /// </summary>
        public long FailedRequests { get; set; }
        
        /// <summary>
        /// 平均处理时间（毫秒）
        /// </summary>
        public double AverageProcessingTime { get; set; }
        
        /// <summary>
        /// 最小处理时间（毫秒）
        /// </summary>
        public long MinProcessingTime { get; set; }
        
        /// <summary>
        /// 最大处理时间（毫秒）
        /// </summary>
        public long MaxProcessingTime { get; set; }
        
        /// <summary>
        /// 当前连接数
        /// </summary>
        public int CurrentConnections { get; set; }
        
        /// <summary>
        /// 最大连接数
        /// </summary>
        public int MaxConnections { get; set; }
        
        /// <summary>
        /// 活跃连接数
        /// </summary>
        public int ActiveConnections { get; set; }
        
        /// <summary>
        /// 注册服务数
        /// </summary>
        public int RegisteredServices { get; set; }
        
        /// <summary>
        /// 运行服务数
        /// </summary>
        public int RunningServices { get; set; }
    }
    
    /// <summary>
    /// 请求处理器接口
    /// </summary>
    public interface IRequestHandler
    {
        /// <summary>
        /// 处理请求
        /// </summary>
        /// <param name="request">请求对象</param>
        /// <returns>响应对象</returns>
        Task<object> HandleRequestAsync(object request);
        
        /// <summary>
        /// 检查是否支持指定请求类型
        /// </summary>
        /// <param name="requestType">请求类型</param>
        /// <returns>true表示支持，false表示不支持</returns>
        bool CanHandle(Type requestType);
    }
    
    /// <summary>
    /// 服务生命周期监听器接口
    /// </summary>
    public interface IServiceLifecycleListener
    {
        /// <summary>
        /// 服务注册事件
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        /// <param name="serviceImpl">服务实现</param>
        void OnServiceRegistered(string serviceName, string version, object serviceImpl);
        
        /// <summary>
        /// 服务注销事件
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        void OnServiceUnregistered(string serviceName, string version);
        
        /// <summary>
        /// 服务启动事件
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        void OnServiceStarted(string serviceName, string version);
        
        /// <summary>
        /// 服务停止事件
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        void OnServiceStopped(string serviceName, string version);
        
        /// <summary>
        /// 服务错误事件
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        /// <param name="exception">异常信息</param>
        void OnServiceError(string serviceName, string version, Exception exception);
    }
} 