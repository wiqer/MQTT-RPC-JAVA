# 改进的RPC框架抽象设计

## 设计原则

基于.NET版本和Java版本的对比分析，提出以下改进的抽象设计原则：

1. **单一职责原则**：每个类只负责一个功能
2. **开闭原则**：对扩展开放，对修改关闭
3. **依赖倒置原则**：依赖抽象而非具体实现
4. **接口隔离原则**：客户端不应该依赖它不需要的接口
5. **组合优于继承**：优先使用组合而非继承

## 核心抽象设计

### 1. 基础接口层

#### 1.1 标记接口
```java
/**
 * RPC服务标记接口
 * 用于标识RPC服务实现类
 */
public interface RpcService {
    // 标记接口，无方法定义
}

/**
 * RPC客户端标记接口
 * 用于标识RPC客户端类
 */
public interface RpcClient {
    // 标记接口，无方法定义
}
```

#### 1.2 序列化接口
```java
/**
 * 序列化接口 - 统一序列化机制
 */
public interface Serializer {
    /**
     * 序列化为字节数组
     */
    byte[] serialize(Object obj);
    
    /**
     * 从字节数组反序列化
     */
    <T> T deserialize(byte[] data, Class<T> clazz);
    
    /**
     * 序列化为字符串
     */
    String serializeToString(Object obj);
    
    /**
     * 从字符串反序列化
     */
    <T> T deserializeFromString(String data, Class<T> clazz);
    
    /**
     * 获取序列化器名称
     */
    String getName();
}
```

#### 1.3 调用处理接口
```java
/**
 * RPC调用处理器接口
 */
public interface RpcInvocationHandler {
    /**
     * 处理RPC调用
     */
    Object invoke(RpcInvocation invocation) throws Throwable;
}

/**
 * RPC调用信息
 */
public class RpcInvocation {
    private final String serviceName;
    private final String methodName;
    private final String version;
    private final Object[] arguments;
    private final Class<?>[] parameterTypes;
    private final Class<?> returnType;
    
    // 构造函数、getter方法...
}
```

### 2. 核心抽象类

#### 2.1 消息映射基类
```java
/**
 * 消息映射基类 - 统一消息处理机制
 */
public abstract class MessageMapper {
    protected final Serializer serializer;
    protected final String version;
    protected final String serviceName;
    protected final Map<String, MethodInfo> methodMap;
    
    protected MessageMapper(Serializer serializer, String version, String serviceName) {
        this.serializer = serializer;
        this.version = version;
        this.serviceName = serviceName;
        this.methodMap = new ConcurrentHashMap<>();
    }
    
    /**
     * 注册方法信息
     */
    protected void registerMethod(String methodName, MethodInfo methodInfo) {
        methodMap.put(methodName, methodInfo);
    }
    
    /**
     * 获取方法信息
     */
    protected MethodInfo getMethod(String methodName) {
        return methodMap.get(methodName);
    }
    
    /**
     * 初始化方法映射
     */
    protected abstract void initializeMethodMapping();
}

/**
 * 方法信息
 */
public class MethodInfo {
    private final String name;
    private final Class<?>[] parameterTypes;
    private final Class<?> returnType;
    private final Method method;
    
    // 构造函数、getter方法...
}
```

#### 2.2 生产者抽象类
```java
/**
 * RPC生产者抽象类
 */
public abstract class RpcProducer extends MessageMapper implements RpcInvocationHandler {
    protected final RpcTransport transport;
    
    protected RpcProducer(Serializer serializer, String version, String serviceName, RpcTransport transport) {
        super(serializer, version, serviceName);
        this.transport = transport;
    }
    
    @Override
    public Object invoke(RpcInvocation invocation) throws Throwable {
        // 1. 序列化请求
        byte[] requestData = serializeRequest(invocation);
        
        // 2. 发送请求
        byte[] responseData = transport.sendRequest(invocation.getServiceName(), requestData);
        
        // 3. 反序列化响应
        return deserializeResponse(responseData, invocation.getReturnType());
    }
    
    protected abstract byte[] serializeRequest(RpcInvocation invocation);
    protected abstract Object deserializeResponse(byte[] data, Class<?> returnType);
}
```

#### 2.3 消费者抽象类
```java
/**
 * RPC消费者抽象类
 */
public abstract class RpcConsumer extends MessageMapper {
    protected final Object serviceInstance;
    protected final RpcTransport transport;
    
    protected RpcConsumer(Serializer serializer, String version, String serviceName, 
                         Object serviceInstance, RpcTransport transport) {
        super(serializer, version, serviceName);
        this.serviceInstance = serviceInstance;
        this.transport = transport;
    }
    
    /**
     * 启动消费者
     */
    public abstract void start();
    
    /**
     * 停止消费者
     */
    public abstract void stop();
    
    /**
     * 处理请求
     */
    protected Object handleRequest(RpcInvocation invocation) throws Throwable {
        MethodInfo methodInfo = getMethod(invocation.getMethodName());
        if (methodInfo == null) {
            throw new RpcException("Method not found: " + invocation.getMethodName());
        }
        
        return methodInfo.getMethod().invoke(serviceInstance, invocation.getArguments());
    }
}
```

### 3. 传输层抽象

#### 3.1 传输接口
```java
/**
 * RPC传输接口
 */
public interface RpcTransport {
    /**
     * 发送请求并等待响应
     */
    byte[] sendRequest(String serviceName, byte[] requestData) throws Exception;
    
    /**
     * 启动传输层
     */
    void start();
    
    /**
     * 停止传输层
     */
    void stop();
    
    /**
     * 设置请求处理器
     */
    void setRequestHandler(RequestHandler handler);
}

/**
 * 请求处理器接口
 */
public interface RequestHandler {
    /**
     * 处理请求
     */
    byte[] handleRequest(String serviceName, byte[] requestData);
}
```

### 4. 代理工厂

#### 4.1 代理工厂接口
```java
/**
 * RPC代理工厂
 */
public interface RpcProxyFactory {
    /**
     * 创建服务代理
     */
    <T> T createProxy(Class<T> serviceInterface, String version);
    
    /**
     * 创建服务实例
     */
    <T> T createService(Class<T> serviceInterface, String version, Object implementation);
}
```

#### 4.2 动态代理实现
```java
/**
 * 动态代理工厂实现
 */
public class DynamicProxyFactory implements RpcProxyFactory {
    private final Map<Class<?>, Object> proxyCache = new ConcurrentHashMap<>();
    private final RpcInvocationHandler invocationHandler;
    
    public DynamicProxyFactory(RpcInvocationHandler invocationHandler) {
        this.invocationHandler = invocationHandler;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T createProxy(Class<T> serviceInterface, String version) {
        return (T) proxyCache.computeIfAbsent(serviceInterface, 
            clazz -> Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                new RpcInvocationHandlerAdapter(invocationHandler, version)
            ));
    }
}

/**
 * 调用处理器适配器
 */
class RpcInvocationHandlerAdapter implements InvocationHandler {
    private final RpcInvocationHandler handler;
    private final String version;
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation invocation = new RpcInvocation(
            method.getDeclaringClass().getName(),
            method.getName(),
            version,
            args,
            method.getParameterTypes(),
            method.getReturnType()
        );
        
        return handler.invoke(invocation);
    }
}
```

### 5. 配置抽象

#### 5.1 配置接口
```java
/**
 * RPC配置接口
 */
public interface RpcConfig {
    /**
     * 获取服务名称
     */
    String getServiceName();
    
    /**
     * 获取版本号
     */
    String getVersion();
    
    /**
     * 获取序列化器
     */
    Serializer getSerializer();
    
    /**
     * 获取传输配置
     */
    TransportConfig getTransportConfig();
}

/**
 * 传输配置接口
 */
public interface TransportConfig {
    String getType(); // "rabbitmq", "mqtt", "websocket", "netty"
    Map<String, Object> getProperties();
}
```

### 6. 启动器抽象

#### 6.1 服务启动器
```java
/**
 * RPC服务启动器
 */
public abstract class RpcServerBootstrap {
    protected final RpcConfig config;
    protected final Map<String, Object> services = new ConcurrentHashMap<>();
    
    public RpcServerBootstrap(RpcConfig config) {
        this.config = config;
    }
    
    /**
     * 注册服务
     */
    public <T> RpcServerBootstrap registerService(Class<T> serviceInterface, T implementation) {
        services.put(serviceInterface.getName(), implementation);
        return this;
    }
    
    /**
     * 启动服务
     */
    public abstract RpcServer start();
}

/**
 * RPC客户端启动器
 */
public abstract class RpcClientBootstrap {
    protected final RpcConfig config;
    
    public RpcClientBootstrap(RpcConfig config) {
        this.config = config;
    }
    
    /**
     * 创建服务代理
     */
    public abstract <T> T createService(Class<T> serviceInterface);
    
    /**
     * 启动客户端
     */
    public abstract RpcClient start();
}
```

## 具体实现示例

### RabbitMQ实现
```java
/**
 * RabbitMQ传输实现
 */
public class RabbitMQTransport implements RpcTransport {
    private final ConnectionFactory factory;
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();
    private RequestHandler requestHandler;
    
    @Override
    public byte[] sendRequest(String serviceName, byte[] requestData) throws Exception {
        Channel channel = getChannel(serviceName);
        String replyQueueName = channel.queueDeclare().getQueue();
        
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
            .correlationId(UUID.randomUUID().toString())
            .replyTo(replyQueueName)
            .build();
            
        channel.basicPublish("", serviceName, props, requestData);
        
        // 等待响应...
        return waitForResponse(replyQueueName);
    }
}

/**
 * RabbitMQ生产者
 */
public class RabbitMQProducer extends RpcProducer {
    public RabbitMQProducer(Serializer serializer, String version, String serviceName, 
                           RabbitMQTransport transport) {
        super(serializer, version, serviceName, transport);
    }
    
    @Override
    protected byte[] serializeRequest(RpcInvocation invocation) {
        return serializer.serialize(invocation);
    }
    
    @Override
    protected Object deserializeResponse(byte[] data, Class<?> returnType) {
        return serializer.deserialize(data, returnType);
    }
}
```

## 设计优势

### 1. 清晰的职责分离
- **接口层**：定义契约
- **抽象层**：提供通用实现
- **实现层**：具体协议实现
- **配置层**：统一配置管理

### 2. 更好的扩展性
- 新增协议只需实现 `RpcTransport` 接口
- 新增序列化方式只需实现 `Serializer` 接口
- 支持多种代理实现方式

### 3. 统一的API设计
- 服务端和客户端使用相同的配置方式
- 统一的异常处理机制
- 一致的命名规范

### 4. 更好的可测试性
- 接口分离便于Mock测试
- 配置抽象便于单元测试
- 依赖注入便于集成测试

## 迁移策略

### 1. 渐进式迁移
1. 保持现有API兼容性
2. 逐步引入新的抽象层
3. 提供适配器模式支持旧版本

### 2. 配置兼容
```java
// 旧版本配置
@EFRpcService(version = "v1")
public class OldService implements IMsgServer, MsgController {
    // 实现...
}

// 新版本配置
@RpcService(version = "v1")
public class NewService implements IMsgServer {
    // 实现...
}
```

### 3. 启动方式兼容
```java
// 旧版本启动
new ConsumerBootstrap<Program>().start(new RabbitMQOptionsFactory<>());

// 新版本启动
RpcServer server = new RpcServerBootstrap(config)
    .registerService(IMsgServer.class, new NewService())
    .start();
```

这个改进的设计方案结合了.NET版本的优秀抽象和Java版本的实际需求，提供了更加清晰、可扩展和可维护的RPC框架架构。 