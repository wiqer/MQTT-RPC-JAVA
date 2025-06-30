using System;
using System.Threading.Tasks;

namespace EF.RPC.Sharing
{
    /// <summary>
    /// RPC客户端接口
    /// 遵循统一架构设计规范，为RPC客户端提供标准化的功能定义
    /// </summary>
    public interface IRpcClient : IRpcComponent
    {
        /// <summary>
        /// 获取客户端名称
        /// </summary>
        string ClientName { get; }
        
        /// <summary>
        /// 获取客户端版本
        /// </summary>
        string ClientVersion { get; }
        
        /// <summary>
        /// 创建服务代理
        /// </summary>
        /// <typeparam name="T">服务接口类型</typeparam>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        /// <returns>服务代理对象</returns>
        T CreateProxy<T>(string serviceName, string version);
        
        /// <summary>
        /// 创建服务代理（带配置）
        /// </summary>
        /// <typeparam name="T">服务接口类型</typeparam>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        /// <param name="config">客户端配置</param>
        /// <returns>服务代理对象</returns>
        T CreateProxy<T>(string serviceName, string version, IRpcConfig config);
        
        /// <summary>
        /// 调用远程方法
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="methodName">方法名称</param>
        /// <param name="args">方法参数</param>
        /// <returns>调用结果</returns>
        object Invoke(string serviceName, string methodName, object[] args);
        
        /// <summary>
        /// 异步调用远程方法
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="methodName">方法名称</param>
        /// <param name="args">方法参数</param>
        /// <returns>异步调用结果</returns>
        Task<object> InvokeAsync(string serviceName, string methodName, object[] args);
        
        /// <summary>
        /// 检查服务是否可用
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        /// <returns>true表示可用，false表示不可用</returns>
        bool IsServiceAvailable(string serviceName, string version);
        
        /// <summary>
        /// 异步检查服务是否可用
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        /// <returns>异步检查结果</returns>
        Task<bool> IsServiceAvailableAsync(string serviceName, string version);
        
        /// <summary>
        /// 获取客户端配置
        /// </summary>
        /// <returns>客户端配置</returns>
        IRpcConfig GetConfig();
        
        /// <summary>
        /// 设置客户端配置
        /// </summary>
        /// <param name="config">客户端配置</param>
        void SetConfig(IRpcConfig config);
        
        /// <summary>
        /// 获取连接状态
        /// </summary>
        /// <returns>连接状态</returns>
        ConnectionStatus GetConnectionStatus();
        
        /// <summary>
        /// 获取客户端统计信息
        /// </summary>
        /// <returns>客户端统计信息</returns>
        ClientStats GetStats();
        
        /// <summary>
        /// 注册服务发现监听器
        /// </summary>
        /// <param name="listener">服务发现监听器</param>
        void RegisterServiceDiscoveryListener(IServiceDiscoveryListener listener);
        
        /// <summary>
        /// 注销服务发现监听器
        /// </summary>
        /// <param name="listener">服务发现监听器</param>
        void UnregisterServiceDiscoveryListener(IServiceDiscoveryListener listener);
        
        /// <summary>
        /// 注册连接状态监听器
        /// </summary>
        /// <param name="listener">连接状态监听器</param>
        void RegisterConnectionStatusListener(IConnectionStatusListener listener);
        
        /// <summary>
        /// 注销连接状态监听器
        /// </summary>
        /// <param name="listener">连接状态监听器</param>
        void UnregisterConnectionStatusListener(IConnectionStatusListener listener);
    }
    
    /// <summary>
    /// 连接状态枚举
    /// </summary>
    public enum ConnectionStatus
    {
        /// <summary>
        /// 已断开
        /// </summary>
        Disconnected,
        
        /// <summary>
        /// 连接中
        /// </summary>
        Connecting,
        
        /// <summary>
        /// 已连接
        /// </summary>
        Connected,
        
        /// <summary>
        /// 重连中
        /// </summary>
        Reconnecting,
        
        /// <summary>
        /// 连接失败
        /// </summary>
        Failed
    }
    
    /// <summary>
    /// 客户端统计信息
    /// </summary>
    public class ClientStats
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
        /// 超时请求数
        /// </summary>
        public long TimeoutRequests { get; set; }
        
        /// <summary>
        /// 平均响应时间（毫秒）
        /// </summary>
        public double AverageResponseTime { get; set; }
        
        /// <summary>
        /// 最小响应时间（毫秒）
        /// </summary>
        public long MinResponseTime { get; set; }
        
        /// <summary>
        /// 最大响应时间（毫秒）
        /// </summary>
        public long MaxResponseTime { get; set; }
        
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
        /// 空闲连接数
        /// </summary>
        public int IdleConnections { get; set; }
    }
    
    /// <summary>
    /// 服务发现监听器接口
    /// </summary>
    public interface IServiceDiscoveryListener
    {
        /// <summary>
        /// 服务上线事件
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        /// <param name="endpoint">服务端点</param>
        void OnServiceOnline(string serviceName, string version, string endpoint);
        
        /// <summary>
        /// 服务下线事件
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        /// <param name="endpoint">服务端点</param>
        void OnServiceOffline(string serviceName, string version, string endpoint);
        
        /// <summary>
        /// 服务更新事件
        /// </summary>
        /// <param name="serviceName">服务名称</param>
        /// <param name="version">服务版本</param>
        /// <param name="endpoint">服务端点</param>
        void OnServiceUpdated(string serviceName, string version, string endpoint);
    }
    
    /// <summary>
    /// 连接状态监听器接口
    /// </summary>
    public interface IConnectionStatusListener
    {
        /// <summary>
        /// 连接状态变化事件
        /// </summary>
        /// <param name="oldStatus">旧状态</param>
        /// <param name="newStatus">新状态</param>
        void OnConnectionStatusChanged(ConnectionStatus oldStatus, ConnectionStatus newStatus);
        
        /// <summary>
        /// 连接建立事件
        /// </summary>
        /// <param name="endpoint">连接端点</param>
        void OnConnected(string endpoint);
        
        /// <summary>
        /// 连接断开事件
        /// </summary>
        /// <param name="endpoint">连接端点</param>
        /// <param name="reason">断开原因</param>
        void OnDisconnected(string endpoint, string reason);
        
        /// <summary>
        /// 连接失败事件
        /// </summary>
        /// <param name="endpoint">连接端点</param>
        /// <param name="exception">异常信息</param>
        void OnConnectionFailed(string endpoint, Exception exception);
    }
} 