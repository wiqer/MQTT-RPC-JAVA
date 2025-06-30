# RPC框架统一架构设计

## 设计目标

创建一个跨语言的RPC框架标准，确保Java和.NET版本具有一致的架构设计，为其他语言实现提供标准化的参考。

## 核心设计原则

### 1. 分层架构
```
┌─────────────────────────────────────┐
│           应用层 (Application)       │
├─────────────────────────────────────┤
│           代理层 (Proxy)            │
├─────────────────────────────────────┤
│           序列化层 (Serialization)   │
├─────────────────────────────────────┤
│           传输层 (Transport)        │
├─────────────────────────────────────┤
│           消息队列层 (Message Queue) │
└─────────────────────────────────────┘
```

### 2. 接口设计原则
- **单一职责原则**：每个接口只负责一个功能
- **开闭原则**：对扩展开放，对修改关闭
- **依赖倒置原则**：依赖抽象而非具体实现
- **接口隔离原则**：客户端不应该依赖它不需要的接口

## 统一接口定义

### 1. 核心标记接口

#### 1.1 RPC组件标记接口
```java
// Java版本
public interface RpcComponent {
    String getComponentName();
    String getComponentVersion();
    void initialize();
    void start();
    void stop();
    boolean isRunning();
    ComponentStatus getStatus();
}

// C#版本
public interface IRpcComponent {
    string ComponentName { get; }
    string ComponentVersion { get; }
    void Initialize();
    void Start();
    void Stop();
    bool IsRunning { get; }
    ComponentStatus Status { get; }
}
```

#### 1.2 RPC服务标记接口
```java
// Java版本
public interface RpcService extends RpcComponent {
    String getServiceName();
    String getServiceVersion();
    Class<?> getServiceInterface();
    Class<?> getServiceImplementation();
}

// C#版本
public interface IRpcService : IRpcComponent {
    string ServiceName { get; }
    string ServiceVersion { get; }
    Type ServiceInterface { get; }
    Type ServiceImplementation { get; }
}
```

#### 1.3 RPC客户端标记接口
```java
// Java版本
public interface RpcClient extends RpcComponent {
    String getClientName();
    String getClientVersion();
    <T> T createProxy(Class<T> serviceInterface, String serviceName, String version);
    Object invoke(String serviceName, String methodName, Object[] args);
}

// C#版本
public interface IRpcClient : IRpcComponent {
    string ClientName { get; }
    string ClientVersion { get; }
    T CreateProxy<T>(string serviceName, string version);
    object Invoke(string serviceName, string methodName, object[] args);
}
```

### 2. 序列化接口

```java
// Java版本
public interface Serializer {
    byte[] serialize(Object obj);
    <T> T deserialize(byte[] data, Class<T> clazz);
    String serializeToString(Object obj);
    <T> T deserializeFromString(String data, Class<T> clazz);
    String getSerializerName();
    String getSerializerVersion();
    boolean supportsType(Class<?> clazz);
}

// C#版本
public interface ISerializer {
    byte[] Serialize(object obj);
    T Deserialize<T>(byte[] data);
    string SerializeToString(object obj);
    T DeserializeFromString<T>(string data);
    string SerializerName { get; }
    string SerializerVersion { get; }
    bool SupportsType(Type type);
}
```

### 3. 传输层接口

```java
// Java版本
public interface RpcTransport {
    byte[] sendRequest(String serviceName, byte[] requestData) throws Exception;
    void start();
    void stop();
    void setRequestHandler(RequestHandler handler);
    boolean isConnected();
    TransportStatus getStatus();
}

// C#版本
public interface IRpcTransport {
    Task<byte[]> SendRequestAsync(string serviceName, byte[] requestData);
    void Start();
    void Stop();
    void SetRequestHandler(IRequestHandler handler);
    bool IsConnected { get; }
    TransportStatus Status { get; }
}
```

### 4. 代理工厂接口

```java
// Java版本
public interface RpcProxyFactory {
    <T> T createProxy(Class<T> serviceInterface, String version);
    <T> T createService(Class<T> serviceInterface, String version, Object implementation);
    void clearCache();
    CacheStats getCacheStats();
}

// C#版本
public interface IRpcProxyFactory {
    T CreateProxy<T>(string version);
    T CreateService<T>(string version, object implementation);
    void ClearCache();
    CacheStats GetCacheStats();
}
```

## 统一配置管理

### 1. 配置接口

```java
// Java版本
public interface RpcConfig {
    String getServiceName();
    String getVersion();
    Serializer getSerializer();
    TransportConfig getTransportConfig();
    long getTimeout();
    String getRunMode();
    boolean isEnableCache();
    boolean isEnableLogging();
}

// C#版本
public interface IRpcConfig {
    string ServiceName { get; }
    string Version { get; }
    ISerializer Serializer { get; }
    ITransportConfig TransportConfig { get; }
    long Timeout { get; }
    string RunMode { get; }
    bool EnableCache { get; }
    bool EnableLogging { get; }
}
```

### 2. 传输配置

```java
// Java版本
public interface TransportConfig {
    String getType(); // "rabbitmq", "mqtt", "websocket", "netty"
    Map<String, Object> getProperties();
    String getHost();
    int getPort();
    String getUsername();
    String getPassword();
}

// C#版本
public interface ITransportConfig {
    string Type { get; }
    Dictionary<string, object> Properties { get; }
    string Host { get; }
    int Port { get; }
    string Username { get; }
    string Password { get; }
}
```

## 统一异常处理

### 1. RPC异常类

```java
// Java版本
public class RpcException extends RuntimeException {
    private final String errorCode;
    private final String serviceName;
    private final String methodName;
    private final long timestamp;
    
    public RpcException(String errorCode, String message, String serviceName, String methodName) {
        super(message);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.timestamp = System.currentTimeMillis();
    }
    
    // getter方法...
}

// C#版本
public class RpcException : Exception {
    public string ErrorCode { get; }
    public string ServiceName { get; }
    public string MethodName { get; }
    public long Timestamp { get; }
    
    public RpcException(string errorCode, string message, string serviceName, string methodName) 
        : base(message) {
        ErrorCode = errorCode;
        ServiceName = serviceName;
        MethodName = methodName;
        Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
    }
}
```

## 统一注解/特性定义

### 1. 服务注解

```java
// Java版本
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {
    String version() default "v1";
    Class<?> serviceInterface() default Object.class;
    String serviceName() default "";
    boolean enabled() default true;
}

// C#版本
[AttributeUsage(AttributeTargets.Class)]
public class RpcServiceAttribute : Attribute {
    public string Version { get; set; } = "v1";
    public Type ServiceInterface { get; set; }
    public string ServiceName { get; set; } = "";
    public bool Enabled { get; set; } = true;
}
```

### 2. 方法注解

```java
// Java版本
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcMethod {
    String name() default "";
    String version() default "";
    long timeout() default 5000;
    boolean async() default false;
}

// C#版本
[AttributeUsage(AttributeTargets.Method)]
public class RpcMethodAttribute : Attribute {
    public string Name { get; set; } = "";
    public string Version { get; set; } = "";
    public long Timeout { get; set; } = 5000;
    public bool Async { get; set; } = false;
}
```

## 统一启动器设计

### 1. 服务启动器

```java
// Java版本
public abstract class RpcServerBootstrap {
    protected final RpcConfig config;
    protected final Map<String, Object> services = new ConcurrentHashMap<>();
    
    public RpcServerBootstrap(RpcConfig config) {
        this.config = config;
    }
    
    public <T> RpcServerBootstrap registerService(Class<T> serviceInterface, T implementation) {
        services.put(serviceInterface.getName(), implementation);
        return this;
    }
    
    public abstract RpcServer start();
}

// C#版本
public abstract class RpcServerBootstrap {
    protected readonly IRpcConfig config;
    protected readonly ConcurrentDictionary<string, object> services = new ConcurrentDictionary<string, object>();
    
    public RpcServerBootstrap(IRpcConfig config) {
        this.config = config;
    }
    
    public RpcServerBootstrap RegisterService<T>(T implementation) {
        services.TryAdd(typeof(T).Name, implementation);
        return this;
    }
    
    public abstract IRpcServer Start();
}
```

### 2. 客户端启动器

```java
// Java版本
public abstract class RpcClientBootstrap {
    protected final RpcConfig config;
    
    public RpcClientBootstrap(RpcConfig config) {
        this.config = config;
    }
    
    public abstract <T> T createService(Class<T> serviceInterface);
    public abstract RpcClient start();
}

// C#版本
public abstract class RpcClientBootstrap {
    protected readonly IRpcConfig config;
    
    public RpcClientBootstrap(IRpcConfig config) {
        this.config = config;
    }
    
    public abstract T CreateService<T>();
    public abstract IRpcClient Start();
}
```

## 统一消息格式

### 1. RPC请求消息

```java
// Java版本
public class RpcRequest {
    private String requestId;
    private String serviceName;
    private String methodName;
    private String version;
    private Object[] arguments;
    private Class<?>[] parameterTypes;
    private Class<?> returnType;
    private long timestamp;
    private Map<String, Object> metadata;
    
    // 构造函数、getter/setter方法...
}

// C#版本
public class RpcRequest {
    public string RequestId { get; set; }
    public string ServiceName { get; set; }
    public string MethodName { get; set; }
    public string Version { get; set; }
    public object[] Arguments { get; set; }
    public Type[] ParameterTypes { get; set; }
    public Type ReturnType { get; set; }
    public long Timestamp { get; set; }
    public Dictionary<string, object> Metadata { get; set; }
}
```

### 2. RPC响应消息

```java
// Java版本
public class RpcResponse {
    private String requestId;
    private Object result;
    private Throwable exception;
    private long timestamp;
    private Map<String, Object> metadata;
    
    // 构造函数、getter/setter方法...
}

// C#版本
public class RpcResponse {
    public string RequestId { get; set; }
    public object Result { get; set; }
    public Exception Exception { get; set; }
    public long Timestamp { get; set; }
    public Dictionary<string, object> Metadata { get; set; }
}
```

## 统一监控和日志

### 1. 监控接口

```java
// Java版本
public interface RpcMonitor {
    void recordRequest(String serviceName, String methodName, long duration);
    void recordError(String serviceName, String methodName, Throwable error);
    void recordSuccess(String serviceName, String methodName);
    MonitorStats getStats();
}

// C#版本
public interface IRpcMonitor {
    void RecordRequest(string serviceName, string methodName, long duration);
    void RecordError(string serviceName, string methodName, Exception error);
    void RecordSuccess(string serviceName, string methodName);
    MonitorStats GetStats();
}
```

### 2. 日志接口

```java
// Java版本
public interface RpcLogger {
    void debug(String message, Object... args);
    void info(String message, Object... args);
    void warn(String message, Object... args);
    void error(String message, Throwable error);
}

// C#版本
public interface IRpcLogger {
    void Debug(string message, params object[] args);
    void Info(string message, params object[] args);
    void Warn(string message, params object[] args);
    void Error(string message, Exception error);
}
```

## 实现规范

### 1. 命名规范
- **Java版本**：使用驼峰命名法，接口以I开头（可选）
- **C#版本**：使用Pascal命名法，接口以I开头

### 2. 包/命名空间规范
- **Java版本**：`com.wiqer.rpc.{module}`
- **C#版本**：`EF.RPC.{Module}`

### 3. 版本兼容性
- 主版本号：不兼容的API修改
- 次版本号：向下兼容的功能性新增
- 修订号：向下兼容的问题修正

### 4. 文档规范
- 所有公共接口必须有完整的文档注释
- 提供使用示例和最佳实践
- 包含性能基准测试结果

## 测试规范

### 1. 单元测试
- 每个公共方法必须有对应的单元测试
- 测试覆盖率不低于80%
- 包含正常流程和异常流程测试

### 2. 集成测试
- 提供完整的端到端测试示例
- 包含不同传输协议的测试
- 包含性能压力测试

### 3. 基准测试
- 提供性能基准测试
- 包含吞吐量和延迟测试
- 提供不同场景下的性能对比

## 部署规范

### 1. 打包规范
- **Java版本**：Maven/Gradle构建，支持JAR包发布
- **C#版本**：NuGet包发布，支持.NET Standard

### 2. 配置规范
- 支持配置文件和环境变量
- 提供默认配置和自定义配置
- 支持配置热更新

### 3. 监控规范
- 提供健康检查接口
- 支持指标收集和导出
- 提供告警机制

这个统一架构设计确保了Java和.NET版本具有一致的接口定义和实现规范，为其他语言实现提供了标准化的参考。 