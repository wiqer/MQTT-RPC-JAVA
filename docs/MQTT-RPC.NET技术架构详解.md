# MQTT-RPC.NET 技术架构详解

## 1. 项目结构分析

### 1.1 模块划分

```
EFRPC.NET/
├── EF.RPC.Client/           # 客户端启动模块
├── EF.RPC.Server/           # 服务端启动模块
├── EF.RPC.Sharing/          # 共享接口定义
├── EF.RPC.Impl/             # 核心实现模块
│   ├── annotation/          # 注解定义
│   ├── DynamicProxy/        # 动态代理实现
│   ├── ProducerImpl/        # 生产者实现
│   ├── ConsumerImpl/        # 消费者实现
│   └── Concurrent/          # 并发控制
├── EF.RPC.Impl.RabbitMQImpl/ # RabbitMQ 协议实现
├── EF.RPC.Impl.WebSocketImpl/ # WebSocket 协议实现
├── EF.RPC.Protobuf/         # 序列化模块
└── EF.RCP.Test/             # 测试模块
```

### 1.2 核心依赖关系

```
EF.RPC.Client/Server
    ↓
EF.RPC.Impl
    ↓
EF.RPC.Impl.RabbitMQImpl/WebSocketImpl
    ↓
EF.RPC.Protobuf
    ↓
EF.RPC.Sharing
```

## 2. 核心设计模式

### 2.1 工厂模式

**DynamicProxyFactory** - 动态代理工厂
- 负责创建和管理动态代理类
- 实现代理类的缓存机制
- 提供接口代理和类代理两种方式

### 2.2 策略模式

**通信协议策略**
- `RabbitMQMsgProducerMap` - RabbitMQ 生产者策略
- `RabbitMQMsgConsumerMap` - RabbitMQ 消费者策略
- `WebSocketMsgProducerMap` - WebSocket 生产者策略
- `WebSocketMsgConsumerMap` - WebSocket 消费者策略

### 2.3 模板方法模式

**Bootstrap 启动模板**
- `ProducerBootstrap` - 客户端启动模板
- `ConsumerBootstrap` - 服务端启动模板

### 2.4 观察者模式

**消息回调机制**
```csharp
callbackConsumer.Received += (model, ea) =>
{
    // 处理接收到的消息
};
```

## 3. 关键技术实现

### 3.1 动态代理技术

#### 3.1.1 Emit IL 代码生成

**程序集创建：**
```csharp
var assemblyBuilder = AssemblyBuilder.DefineDynamicAssembly(
    new AssemblyName(AssemblyName + type.Name), 
    AssemblyBuilderAccess.Run);
var moduleBuilder = assemblyBuilder.DefineDynamicModule(ModuleName + type.Name);
var typeBuilder = moduleBuilder.DefineType(
    TypeName + type.Name + info.Count,
    TypeAttributes.Public | TypeAttributes.Class, 
    parent, 
    interfaces);
```

**字段定义：**
```csharp
var handlerFieldBuilder = typeBuilder.DefineField(
    "_handler", 
    typeof(InvocationHandlerInterface), 
    FieldAttributes.Private);
var methodInfosFieldBuilder = typeBuilder.DefineField(
    "_methodInfos", 
    typeof(MethodInfo), 
    FieldAttributes.Private);
```

**构造函数生成：**
```csharp
var constructorBuilder = typeBuilder.DefineConstructor(
    MethodAttributes.Public, 
    CallingConventions.Standard,
    new[] { typeof(InvocationHandlerInterface), typeof(MethodInfo[]) });
var ilCtor = constructorBuilder.GetILGenerator();

// 生成构造函数 IL 代码
ilCtor.Emit(OpCodes.Ldarg_0);
ilCtor.Emit(OpCodes.Call, typeof(object).GetConstructor(new Type[0]));
ilCtor.Emit(OpCodes.Ldarg_0);
ilCtor.Emit(OpCodes.Ldarg_1);
ilCtor.Emit(OpCodes.Stfld, handlerFieldBuilder);
ilCtor.Emit(OpCodes.Ret);
```

**方法代理生成：**
```csharp
var methodBuilder = typeBuilder.DefineMethod(
    methodInfo.Name,
    MethodAttributes.Public | MethodAttributes.Virtual,
    methodInfo.CallingConvention, 
    methodInfo.ReturnType, 
    parameterTypes);

var ilMethod = methodBuilder.GetILGenerator();

// 加载 handler 实例
ilMethod.Emit(OpCodes.Ldarg_0);
ilMethod.Emit(OpCodes.Ldfld, handlerFieldBuilder);

// 加载参数数组
ilMethod.Emit(OpCodes.Ldc_I4, parameterTypes.Length);
ilMethod.Emit(OpCodes.Newarr, typeof(object));

// 填充参数数组
for (var j = 0; j < parameterTypes.Length; j++)
{
    ilMethod.Emit(OpCodes.Dup);
    ilMethod.Emit(OpCodes.Ldc_I4_S, (short)j);
    ilMethod.Emit(OpCodes.Ldarg_S, (short)(j + 1));
    
    if (CanBox.Contains(parameterTypes[j]))
    {
        ilMethod.Emit(OpCodes.Box, parameterTypes[j]);
    }
    ilMethod.Emit(OpCodes.Stelem_Ref);
}

// 调用 invoke 方法
ilMethod.Emit(OpCodes.Callvirt, handlerInvokeMethodInfo);
ilMethod.Emit(OpCodes.Ret);
```

#### 3.1.2 代理类缓存机制

```csharp
private static readonly Dictionary<Type, ProxyTypeInfo> ProxyDict = 
    new Dictionary<Type, ProxyTypeInfo>();

public static object createProxyByInterface(Type type, InvocationHandlerInterface handler, bool userCache = true)
{
    if (!userCache || !ProxyDict.TryGetValue(type, out var info))
    {
        // 创建新的代理类型
        var typeBuilder = createDynamicTypeBuilder(type, null, new[] { type });
        var methodInfos = type.GetMethods();
        proxyInit(type, typeBuilder, methodInfos, handlerInvokeMethodInfo);
        
        info = ProxyDict[type];
        if (info.Count == 1)
        {
            info.MethodInfos = methodInfos;
        }
    }
    
    return Activator.CreateInstance(info.TypeBuilder.CreateTypeInfo(), handler, info.MethodInfos);
}
```

### 3.2 消息序列化机制

#### 3.2.1 消息结构设计

**基础消息类：**
```csharp
public abstract class BaseMsg
{
    [JsonProperty]
    public string Id { get; set; }
    
    [JsonProperty]
    public DateTime CreateDate { get; set; }
    
    public BaseMsg()
    {
        Id = SnowflakeId.NextId();
        CreateDate = DateTime.Now;
    }
}
```

**多参数消息类：**
```csharp
public class SuperMsgMulti : BaseMsg
{
    [JsonProperty]
    public object[] msg { get; private set; }  // 请求参数数组
    
    [JsonProperty]
    public object req { get; private set; }    // 响应结果
    
    public SuperMsgMulti setMsg(object[] msg)
    {
        this.msg = msg;
        return this;
    }
    
    public SuperMsgMulti setReq(object req)
    {
        this.msg = null;
        this.req = req;
        return this;
    }
}
```

#### 3.2.2 序列化接口设计

```csharp
public interface SerializerInterface
{
    object DeSerializeBytes(Type type, byte[] content);
    T DeSerializeBytes<T>(byte[] content);
    T DeSerializeString<T>(string content);
    object DeSerializeString(Type type, string content);
    byte[] SerializeBytes(object t);
    string SerializeString(object t);
}
```

**JSON 序列化实现：**
```csharp
public class JosnSerializer : SerializerInterface
{
    public T DeSerializeBytes<T>(byte[] content)
    {
        return JsonConvert.DeserializeObject<T>(Encoding.UTF8.GetString(content));
    }
    
    public byte[] SerializeBytes(object t)
    {
        return Encoding.UTF8.GetBytes(JsonConvert.SerializeObject(t));
    }
    
    public string SerializeString(object t)
    {
        return JsonConvert.SerializeObject(t);
    }
}
```

### 3.3 线程同步机制

#### 3.3.1 Synchronizer 抽象类

```csharp
public abstract class Synchronizer
{
    protected Thread t;
    protected int sleeptime = 0;
    
    public Synchronizer() 
    {
        t = Thread.CurrentThread;
    }
    
    public void acquire()
    {
        if (tryAcquire()) 
        {
            try
            {
                if (sleeptime > 0) 
                { 
                    Thread.Sleep(sleeptime); 
                }
                else
                {
                    Thread.Sleep(Timeout.Infinite);
                }
            }
            catch (ThreadInterruptedException)
            {
                // 被中断消息唤醒
            } 
        }
    }
    
    public bool release()
    {
        if (tryRelease())
        {
            unparkSuccessor(t);
            return true;
        }
        return false;
    }
    
    private void unparkSuccessor(Thread t)
    {
        if (t.ThreadState == ThreadState.WaitSleepJoin) 
        {
            t.Interrupt();
        }
    }
    
    public abstract bool tryRelease();
    public abstract bool tryAcquire();
}
```

#### 3.3.2 UnsafeSynchronizer 实现

```csharp
public class UnsafeSynchronizer : Synchronizer
{
    public override bool tryAcquire()
    {
        return t.ThreadState == ThreadState.Running || 
               t.ThreadState == ThreadState.Background;
    }

    public override bool tryRelease()
    {
        return null != t && t.ThreadState == ThreadState.WaitSleepJoin;
    }
}
```

#### 3.3.3 同步器管理

```csharp
public class RabbitMQMsgFun : MsgFun
{
    public ConcurrentDictionary<string, UnsafeSynchronizer> uscd;
    public ConcurrentDictionary<string, SuperMsgMulti> msgcd;
    
    public void initUnsafeSynchronizer() 
    {
        uscd = new ConcurrentDictionary<string, UnsafeSynchronizer>();
        msgcd = new ConcurrentDictionary<string, SuperMsgMulti>();
    }
    
    public void acquire(string id) 
    {
        UnsafeSynchronizer unsafeSynchronizer = new UnsafeSynchronizer(10000);
        uscd.GetOrAdd(id, unsafeSynchronizer).acquire();
    }
    
    public bool release(string id) 
    {
        UnsafeSynchronizer unsafeSynchronizer;
        try
        {
            uscd.TryGetValue(id, out unsafeSynchronizer);
            if (null != unsafeSynchronizer)
            {
                return unsafeSynchronizer.release();
            }
            return false;
        }
        finally 
        {
            uscd.TryRemove(id, out unsafeSynchronizer);
        }
    }
}
```

## 4. 通信协议实现

### 4.1 RabbitMQ RPC 模式详解

#### 4.1.1 队列命名规范

**服务端队列：** `version + interfaceFullName + "." + methodName`
**客户端临时队列：** 自动生成的唯一队列名

#### 4.1.2 消息属性设置

```csharp
IBasicProperties properties = channel.CreateBasicProperties();
properties.ReplyTo = replyQueue;        // 回复队列名
properties.CorrelationId = correlationId; // 关联ID
properties.Persistent = false;          // 非持久化
```

#### 4.1.3 消息路由机制

**请求路由：**
- 客户端根据方法名构造目标队列名
- 发送消息到对应的服务端队列

**响应路由：**
- 服务端从消息属性中获取 `ReplyTo` 队列名
- 将响应发送到客户端的临时队列

### 4.2 错误处理机制

#### 4.2.1 超时处理

```csharp
public UnsafeSynchronizer(int i) : base(i)
{
    sleeptime = i; // 设置超时时间（毫秒）
}
```

#### 4.2.2 异常传播

```csharp
try
{
    object rep = mfs.methodInfo.Invoke(this.ControllerObj, objs);
    // 处理正常响应
}
catch (Exception ex)
{
    // 处理异常情况
    throw ex;
}
```

## 5. 性能优化策略

### 5.1 缓存优化

**代理类缓存：**
- 相同接口的代理类只生成一次
- 减少动态类型创建的开销

**方法信息缓存：**
- 缓存反射获取的方法信息
- 避免重复的反射操作

### 5.2 连接池管理

**RabbitMQ 连接复用：**
```csharp
IConnection connection = factory.CreateConnection();
IModel channel = connection.CreateModel();
```

### 5.3 序列化优化

**JSON 序列化：**
- 使用 Newtonsoft.Json 进行高效序列化
- 支持对象图的循环引用处理

## 6. 扩展性设计

### 6.1 协议扩展

**新增协议支持：**
1. 实现 `MsgProducerMap` 和 `MsgConsumerMap` 接口
2. 实现对应的 `Options` 和 `OptionsFactory`
3. 在启动时指定新的协议实现

### 6.2 序列化扩展

**新增序列化格式：**
1. 实现 `SerializerInterface` 接口
2. 在消息处理时指定序列化器

### 6.3 注解扩展

**自定义注解：**
1. 继承 `Attribute` 类
2. 在扫描时处理自定义注解逻辑

## 7. 最佳实践

### 7.1 服务定义

```csharp
// 共享接口
public interface IUserService
{
    User GetUser(int id);
    List<User> GetUsers();
}

// 服务实现
[EFRpcService(version = "v1.0")]
public class UserServiceImpl : IUserService, MsgController
{
    public User GetUser(int id)
    {
        // 业务逻辑实现
        return new User { Id = id, Name = "User" + id };
    }
}
```

### 7.2 客户端使用

```csharp
public class UserClientImpl : IUserClient, MsgController
{
    [EFRpcAutowired(version = "v1.0", runMode = "Auto")]
    public IUserService UserService;
    
    public User GetUser(int id)
    {
        return UserService.GetUser(id);
    }
}
```

### 7.3 配置管理

```csharp
// RabbitMQ 配置
var options = new RabbitMQOptions
{
    factory = new ConnectionFactory
    {
        HostName = "localhost",
        Port = 5672,
        UserName = "guest",
        Password = "guest"
    }
};
```

## 8. 总结

MQTT-RPC.NET 通过以下技术特点实现了轻量级的 RPC 框架：

1. **动态代理技术**：使用 Emit 实现透明的远程调用
2. **注解驱动**：简化配置，提高开发效率
3. **协议抽象**：支持多种通信协议
4. **线程同步**：实现可靠的请求-响应模式
5. **缓存优化**：提高性能和资源利用率

该框架为分布式系统提供了简单易用的 RPC 解决方案，特别适合中小型项目的快速开发。 