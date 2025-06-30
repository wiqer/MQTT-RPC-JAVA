# EFRPCJAVA 使用示例

## 项目概述

EFRPCJAVA 是基于 MQTT-RPC.NET 的 Java 版本实现，提供了完整的 RPC 框架功能，包括：

- **注解驱动**：`@EFRpcService` 和 `@EFRpcAutowired` 注解
- **IOC容器**：自动扫描、注册、注入依赖
- **动态代理**：透明化远程调用
- **同步机制**：请求-响应线程同步
- **序列化增强**：支持泛型和多种格式

## 核心组件

### 1. IOC容器与注解扫描

#### IOCContainer
```java
// 获取IOC容器实例
IOCContainer container = IOCContainer.getInstance();

// 注册Bean
container.registerBean("serviceName", serviceInstance);

// 获取Bean
Object service = container.getBean("serviceName");
MyService myService = container.getBean(MyService.class);
```

#### AnnotationScanner
```java
// 创建注解扫描器
AnnotationScanner scanner = new AnnotationScanner();

// 扫描指定包
scanner.scanPackage("com.wiqer.rpc");
```

### 2. 动态代理工厂

#### DynamicProxyFactory
```java
// 创建代理工厂
DynamicProxyFactory proxyFactory = new DynamicProxyFactory();

// 创建RPC代理
IMyService proxy = proxyFactory.createRpcProxy(
    IMyService.class, 
    "v1.", 
    new RpcHandler() {
        @Override
        public Object handleRpcCall(MethodInfo methodInfo) throws Throwable {
            // 处理RPC调用
            return null;
        }
    }
);
```

### 3. 同步器

#### SynchronizerManager
```java
// 创建同步器管理器
SynchronizerManager syncManager = new SynchronizerManager();

// 创建同步器
String requestId = "req_001";
SynchronizerManager.UnsafeSynchronizer synchronizer = 
    syncManager.createSynchronizer(requestId, 30000);

// 等待响应
synchronizer.acquire();

// 释放同步器
syncManager.releaseSynchronizer(requestId);
```

## 使用示例

### 1. 服务端开发

#### 定义服务接口
```java
public interface IMsgServer {
    GetMsgSumReply GetSum(GetMsgNumRequest request);
    int GetNum(int num1, int num2);
    String SendMessage(String message);
}
```

#### 实现服务
```java
@Service
@EFRpcService(version = "v1.", strategyType = IMsgServer.class)
public class MsgServiceImpl implements IMsgServer {
    
    @Override
    public GetMsgSumReply GetSum(GetMsgNumRequest request) {
        GetMsgSumReply reply = new GetMsgSumReply();
        reply.setSum(request.getNum1() + request.getNum2());
        return reply;
    }
    
    @Override
    public int GetNum(int num1, int num2) {
        return num1 + num2;
    }
    
    @Override
    public String SendMessage(String message) {
        return "服务器收到消息: " + message;
    }
}
```

#### 启动服务端
```java
// 创建服务端启动引导
ConsumerBootstrap<Program> bootstrap = new ConsumerBootstrap<>();

// 启动服务端
bootstrap.scanPackage("com.wiqer.rpc.server")
        .start(new ConsumerBootstrap.ConsumerOptions("com.wiqer.rpc.server"));
```

### 2. 客户端开发

#### 定义客户端接口
```java
public interface IMsgClient {
    int GetSum(int num1, int num2);
    int GetNum(int num1, int num2);
    String SendMessage(String message);
}
```

#### 实现客户端
```java
@Service
public class MsgClientImpl implements IMsgClient {
    
    @EFRpcAutowired(version = "v1.")
    public IMsgServer ServiceAutoUpdate;
    
    @Override
    public int GetSum(int num1, int num2) {
        GetMsgNumRequest request = new GetMsgNumRequest(num1, num2);
        GetMsgSumReply reply = ServiceAutoUpdate.GetSum(request);
        return reply.getSum();
    }
    
    @Override
    public int GetNum(int num1, int num2) {
        return ServiceAutoUpdate.GetNum(num1, num2);
    }
    
    @Override
    public String SendMessage(String message) {
        return ServiceAutoUpdate.SendMessage(message);
    }
}
```

#### 启动客户端
```java
// 创建客户端启动引导
ProducerBootstrap<Program> bootstrap = new ProducerBootstrap<>();

// 启动客户端
MsgClientImpl msgClient = bootstrap.scanPackage("com.wiqer.rpc.client")
        .start(new ProducerBootstrap.ProducerOptions("com.wiqer.rpc.client"))
        .getController(MsgClientImpl.class);

// 调用远程服务
int result = msgClient.GetSum(10, 20);
System.out.println("结果: " + result);
```

## 配置说明

### 1. 注解配置

#### @EFRpcService
- `version`: 服务版本号
- `strategyType`: 策略类型（接口类）

#### @EFRpcAutowired
- `version`: 依赖版本号
- `runMode`: 运行模式（auto, syn, asyn）

### 2. 启动配置

#### ConsumerOptions（服务端）
- `basePackage`: 扫描包路径
- `msgHandler`: 消息处理器

#### ProducerOptions（客户端）
- `basePackage`: 扫描包路径
- `msgHandler`: 消息处理器

## 高级功能

### 1. 自定义序列化

```java
// 使用增强的JSON序列化器
JsonSerializer serializer = JsonSerializer.getSerializer();

// 泛型反序列化
List<String> list = serializer.DeSerializeString(jsonString, 
    new TypeReference<List<String>>(){}.getType());

// 检查序列化支持
boolean supported = serializer.isSerializable(MyClass.class);

// 获取序列化大小
int size = serializer.getSerializedSize(myObject);
```

### 2. 同步器管理

```java
// 创建同步器管理器
SynchronizerManager syncManager = new SynchronizerManager();

// 定期清理超时同步器
Timer timer = new Timer();
timer.scheduleAtFixedRate(new TimerTask() {
    @Override
    public void run() {
        syncManager.cleanupTimeoutSynchronizers();
    }
}, 0, 60000); // 每分钟清理一次
```

### 3. 缓存管理

```java
// 获取IOC容器
IOCContainer container = IOCContainer.getInstance();

// 清除所有缓存
container.clearCache();

// 获取代理工厂
DynamicProxyFactory proxyFactory = new DynamicProxyFactory();

// 清除代理缓存
proxyFactory.clearCache();
```

## 最佳实践

### 1. 包结构组织
```
com.wiqer.rpc
├── server          // 服务端实现
│   ├── service     // 服务接口
│   └── impl        // 服务实现
├── client          // 客户端实现
│   ├── controller  // 控制器
│   └── impl        // 客户端实现
└── sharing         // 共享接口和模型
    ├── api         // API接口
    └── model       // 数据模型
```

### 2. 版本管理
- 使用语义化版本号（如：v1.0, v2.1）
- 在注解中明确指定版本
- 保持客户端和服务端版本一致

### 3. 异常处理
```java
try {
    int result = client.GetSum(10, 20);
} catch (RuntimeException e) {
    log.error("RPC调用失败", e);
    // 处理异常
}
```

### 4. 日志配置
```java
// 配置日志级别
logging.level.com.wiqer.rpc=DEBUG
logging.level.com.wiqer.rpc.impl.ioc=INFO
logging.level.com.wiqer.rpc.impl.proxy=DEBUG
```

## 总结

EFRPCJAVA 提供了完整的 RPC 框架功能，通过注解驱动和自动注入简化了开发流程。主要特点包括：

1. **简单易用**：注解驱动，配置简单
2. **功能完整**：支持同步/异步调用、版本管理、缓存等
3. **扩展性强**：支持多种通信协议和序列化方式
4. **性能优化**：多级缓存、连接池等优化

通过以上示例，可以快速上手 EFRPCJAVA 框架，构建分布式 RPC 应用。 