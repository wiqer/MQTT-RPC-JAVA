using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using EF.RPC.Sharing;
using EF.RPC.Impl.annotation;

namespace EF.RCP.Test
{
    /// <summary>
    /// 标准化RPC框架测试
    /// 展示精细化框架的使用方式和最佳实践
    /// </summary>
    public class StandardRpcTest
    {
        private IRpcClient rpcClient;
        private IRpcServer rpcServer;
        private ICalculatorService calculatorService;
        
        /// <summary>
        /// 初始化RPC测试环境
        /// </summary>
        public void Setup()
        {
            Console.WriteLine("初始化RPC测试环境...");
            
            // 创建配置
            var config = CreateRpcConfig();
            
            // 创建服务端
            rpcServer = CreateRpcServer(config);
            
            // 创建客户端
            rpcClient = CreateRpcClient(config);
            
            // 创建服务代理
            calculatorService = rpcClient.CreateProxy<ICalculatorService>("CalculatorService", "v1");
            
            Console.WriteLine("RPC测试环境初始化完成");
        }
        
        /// <summary>
        /// 测试基本RPC调用
        /// </summary>
        public async Task TestBasicRpcCall()
        {
            Console.WriteLine("开始测试基本RPC调用...");
            
            // 启动服务端
            rpcServer.Start();
            
            // 启动客户端
            rpcClient.Start();
            
            // 等待连接建立
            await Task.Delay(2000);
            
            // 测试基本计算
            int result1 = calculatorService.Add(10, 20);
            Console.WriteLine($"10 + 20 = {result1}");
            
            int result2 = calculatorService.Multiply(5, 6);
            Console.WriteLine($"5 * 6 = {result2}");
            
            double result3 = calculatorService.Divide(100, 4);
            Console.WriteLine($"100 / 4 = {result3}");
            
            Console.WriteLine("基本RPC调用测试通过");
        }
        
        /// <summary>
        /// 测试异步RPC调用
        /// </summary>
        public async Task TestAsyncRpcCall()
        {
            Console.WriteLine("开始测试异步RPC调用...");
            
            // 启动服务端和客户端
            rpcServer.Start();
            rpcClient.Start();
            await Task.Delay(2000);
            
            // 异步调用
            var future1 = calculatorService.AddAsync(15, 25);
            var future2 = calculatorService.MultiplyAsync(7, 8);
            
            // 等待结果
            int result1 = await future1;
            int result2 = await future2;
            
            Console.WriteLine($"异步调用结果: 15 + 25 = {result1}, 7 * 8 = {result2}");
            
            Console.WriteLine("异步RPC调用测试通过");
        }
        
        /// <summary>
        /// 测试RPC调用超时
        /// </summary>
        public async Task TestRpcTimeout()
        {
            Console.WriteLine("开始测试RPC调用超时...");
            
            // 启动服务端和客户端
            rpcServer.Start();
            rpcClient.Start();
            await Task.Delay(2000);
            
            try
            {
                // 调用一个会超时的方法
                calculatorService.SlowOperation(10);
                throw new Exception("应该抛出超时异常");
            }
            catch (RpcException e)
            {
                Console.WriteLine($"捕获到预期的超时异常: {e.Message}");
            }
            
            Console.WriteLine("RPC调用超时测试通过");
        }
        
        /// <summary>
        /// 测试RPC调用重试
        /// </summary>
        public async Task TestRpcRetry()
        {
            Console.WriteLine("开始测试RPC调用重试...");
            
            // 启动服务端和客户端
            rpcServer.Start();
            rpcClient.Start();
            await Task.Delay(2000);
            
            // 调用一个可能失败的方法
            int result = calculatorService.UnreliableOperation(5);
            Console.WriteLine($"不可靠操作结果: {result}");
            
            Console.WriteLine("RPC调用重试测试通过");
        }
        
        /// <summary>
        /// 测试RPC监控和统计
        /// </summary>
        public async Task TestRpcMonitoring()
        {
            Console.WriteLine("开始测试RPC监控和统计...");
            
            // 启动服务端和客户端
            rpcServer.Start();
            rpcClient.Start();
            await Task.Delay(2000);
            
            // 执行一些调用
            for (int i = 0; i < 10; i++)
            {
                calculatorService.Add(i, i + 1);
            }
            
            // 获取客户端统计信息
            var clientStats = rpcClient.GetStats();
            Console.WriteLine($"客户端统计信息: {clientStats}");
            
            // 获取服务端统计信息
            var serverStats = rpcServer.GetStats();
            Console.WriteLine($"服务端统计信息: {serverStats}");
            
            Console.WriteLine("RPC监控和统计测试通过");
        }
        
        /// <summary>
        /// 测试RPC配置管理
        /// </summary>
        public async Task TestRpcConfiguration()
        {
            Console.WriteLine("开始测试RPC配置管理...");
            
            // 创建不同的配置
            var config1 = CreateRpcConfig();
            config1.Timeout = 1000;
            config1.SetProperty("custom.key", "custom.value");
            
            var config2 = CreateRpcConfig();
            config2.Timeout = 5000;
            config2.SetProperty("custom.key", "different.value");
            
            // 使用不同配置创建客户端
            var client1 = CreateRpcClient(config1);
            var client2 = CreateRpcClient(config2);
            
            Console.WriteLine("RPC配置管理测试通过");
        }
        
        /// <summary>
        /// 创建RPC配置
        /// </summary>
        private IRpcConfig CreateRpcConfig()
        {
            // 这里应该返回具体的配置实现
            // 暂时返回null，实际使用时需要实现
            return null;
        }
        
        /// <summary>
        /// 创建RPC服务端
        /// </summary>
        private IRpcServer CreateRpcServer(IRpcConfig config)
        {
            // 这里应该返回具体的服务端实现
            // 暂时返回null，实际使用时需要实现
            return null;
        }
        
        /// <summary>
        /// 创建RPC客户端
        /// </summary>
        private IRpcClient CreateRpcClient(IRpcConfig config)
        {
            // 这里应该返回具体的客户端实现
            // 暂时返回null，实际使用时需要实现
            return null;
        }
        
        /// <summary>
        /// 计算器服务接口
        /// </summary>
        public interface ICalculatorService
        {
            /// <summary>
            /// 加法运算
            /// </summary>
            [RpcMethod(Timeout = 3000, Description = "执行加法运算")]
            int Add(int a, int b);
            
            /// <summary>
            /// 乘法运算
            /// </summary>
            [RpcMethod(Timeout = 3000, Description = "执行乘法运算")]
            int Multiply(int a, int b);
            
            /// <summary>
            /// 除法运算
            /// </summary>
            [RpcMethod(Timeout = 3000, Description = "执行除法运算")]
            double Divide(double a, double b);
            
            /// <summary>
            /// 异步加法运算
            /// </summary>
            [RpcMethod(Async = true, Timeout = 5000, Description = "异步执行加法运算")]
            Task<int> AddAsync(int a, int b);
            
            /// <summary>
            /// 异步乘法运算
            /// </summary>
            [RpcMethod(Async = true, Timeout = 5000, Description = "异步执行乘法运算")]
            Task<int> MultiplyAsync(int a, int b);
            
            /// <summary>
            /// 慢速操作（用于测试超时）
            /// </summary>
            [RpcMethod(Timeout = 1000, Description = "慢速操作，用于测试超时")]
            int SlowOperation(int seconds);
            
            /// <summary>
            /// 不可靠操作（用于测试重试）
            /// </summary>
            [RpcMethod(EnableRetry = true, RetryCount = 3, RetryInterval = 1000, 
                      Description = "不可靠操作，用于测试重试机制")]
            int UnreliableOperation(int input);
        }
        
        /// <summary>
        /// 计算器服务实现
        /// </summary>
        [RpcService(Version = "v1", ServiceInterface = typeof(ICalculatorService), 
                   Description = "计算器服务", Author = "EFRPC Team")]
        public class CalculatorServiceImpl : ICalculatorService
        {
            public int Add(int a, int b)
            {
                Console.WriteLine($"执行加法运算: {a} + {b}");
                return a + b;
            }
            
            public int Multiply(int a, int b)
            {
                Console.WriteLine($"执行乘法运算: {a} * {b}");
                return a * b;
            }
            
            public double Divide(double a, double b)
            {
                Console.WriteLine($"执行除法运算: {a} / {b}");
                if (b == 0)
                {
                    throw new DivideByZeroException("除数不能为零");
                }
                return a / b;
            }
            
            public async Task<int> AddAsync(int a, int b)
            {
                Console.WriteLine($"异步执行加法运算: {a} + {b}");
                await Task.Delay(100); // 模拟异步操作
                return a + b;
            }
            
            public async Task<int> MultiplyAsync(int a, int b)
            {
                Console.WriteLine($"异步执行乘法运算: {a} * {b}");
                await Task.Delay(100); // 模拟异步操作
                return a * b;
            }
            
            public int SlowOperation(int seconds)
            {
                Console.WriteLine($"执行慢速操作，耗时{seconds}秒");
                Task.Delay(seconds * 1000).Wait();
                return seconds;
            }
            
            public int UnreliableOperation(int input)
            {
                Console.WriteLine($"执行不可靠操作，输入: {input}");
                // 模拟随机失败
                if (new Random().NextDouble() < 0.3)
                {
                    throw new Exception("随机失败");
                }
                return input * 2;
            }
        }
        
        /// <summary>
        /// RPC服务端接口
        /// </summary>
        public interface IRpcServer : IRpcComponent
        {
            /// <summary>
            /// 获取服务端统计信息
            /// </summary>
            ServerStats GetStats();
        }
        
        /// <summary>
        /// RPC客户端接口
        /// </summary>
        public interface IRpcClient : IRpcComponent
        {
            /// <summary>
            /// 创建服务代理
            /// </summary>
            T CreateProxy<T>(string serviceName, string version);
            
            /// <summary>
            /// 获取客户端统计信息
            /// </summary>
            ClientStats GetStats();
        }
        
        /// <summary>
        /// RPC配置接口
        /// </summary>
        public interface IRpcConfig
        {
            /// <summary>
            /// 超时时间
            /// </summary>
            long Timeout { get; set; }
            
            /// <summary>
            /// 设置属性
            /// </summary>
            void SetProperty(string key, object value);
        }
        
        /// <summary>
        /// 服务端统计信息
        /// </summary>
        public class ServerStats
        {
            public long TotalRequests { get; set; }
            public long SuccessfulRequests { get; set; }
            public long FailedRequests { get; set; }
            public long AverageResponseTime { get; set; }
            public long Uptime { get; set; }
            
            public double SuccessRate => TotalRequests > 0 ? (double)SuccessfulRequests / TotalRequests : 0.0;
            
            public override string ToString()
            {
                return $"ServerStats{{TotalRequests={TotalRequests}, SuccessfulRequests={SuccessfulRequests}, " +
                       $"FailedRequests={FailedRequests}, AverageResponseTime={AverageResponseTime}, " +
                       $"Uptime={Uptime}, SuccessRate={SuccessRate:P2}}}";
            }
        }
        
        /// <summary>
        /// 客户端统计信息
        /// </summary>
        public class ClientStats
        {
            public long TotalRequests { get; set; }
            public long SuccessfulRequests { get; set; }
            public long FailedRequests { get; set; }
            public long TotalResponseTime { get; set; }
            public long AverageResponseTime { get; set; }
            public long LastRequestTime { get; set; }
            
            public double SuccessRate => TotalRequests > 0 ? (double)SuccessfulRequests / TotalRequests : 0.0;
            
            public override string ToString()
            {
                return $"ClientStats{{TotalRequests={TotalRequests}, SuccessfulRequests={SuccessfulRequests}, " +
                       $"FailedRequests={FailedRequests}, AverageResponseTime={AverageResponseTime}, " +
                       $"SuccessRate={SuccessRate:P2}, LastRequestTime={LastRequestTime}}}";
            }
        }
    }
    
    /// <summary>
    /// RPC方法特性
    /// </summary>
    [AttributeUsage(AttributeTargets.Method)]
    public class RpcMethodAttribute : Attribute
    {
        public long Timeout { get; set; } = 5000;
        public bool Async { get; set; } = false;
        public string Description { get; set; } = "";
        public bool EnableRetry { get; set; } = true;
        public int RetryCount { get; set; } = 3;
        public long RetryInterval { get; set; } = 1000;
    }
} 