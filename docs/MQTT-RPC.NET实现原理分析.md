# MQTT-RPC.NET 实现原理分析

## 项目概述

MQTT-RPC.NET 是一个基于 C# 的轻量级 RPC 框架，支持双向通讯、离线发送、内网穿透的分布式 RPC 注解驱动框架。该框架参考了 Dubbo 的设计思路，使用注解驱动的方式简化服务调用和解耦。

## 核心架构

### 1. 整体架构设计

框架采用分层架构设计：
- **注解层**：提供 `@EFRpcService` 和 `@EFRpcAutowired` 注解
- **代理层**：基于 Emit 实现动态代理
- **通信层**：支持 RabbitMQ 和 WebSocket 协议
- **序列化层**：使用 Newtonsoft.Json 进行序列化

### 2. 核心组件

#### 2.1 注解系统

**EFRpcServiceAttribute**
```csharp
public sealed class EFRpcServiceAttribute : Attribute
{
    public string version { get; set; }
    public Type StrategyType { get; set; }
}
```

**EFRpcAutowiredAttribute**
```csharp
public sealed class EFRpcAutowiredAttribute : Attribute
{
    public string version { get; set; }
    public string runMode { get; set; } // auto, syn, asyn
}
```

#### 2.2 动态代理系统

框架使用 C# 的 Emit（IL 字节码生成）实现动态代理，类似 Java 的 CGLIB：

**核心实现类：DynamicProxyFactory**
- 通过 `AssemblyBuilder.DefineDynamicAssembly` 创建动态程序集
- 使用 `TypeBuilder` 生成代理类型
- 为每个方法生成对应的 IL 代码

**代理类生成模板：**
```csharp
public class DynamicProxy1
{
    private II _handler;
    private MethodInfo _methodInfos;
    
    public DynamicProxy1(II _handler, MethodInfo _methodInfos) {
        this._handler = _handler;
        this._methodInfos = _methodInfos;
    }
    
    object m(string args, string arg2)
    {
        return _handler.invoke(_handler, _methodInfos, new object[] { args, arg2 });
    }
}
```

#### 2.3 IOC 容器

**三级缓存设计：**
- **一级缓存**：`DynamicProxyFactory` - 生产单例和缓存单例
- **二级缓存**：`LinkMap + MsgMathsInfoFactory` - 方法信息缓存
- **三级缓存**：未实现（无使用场景）

**DI 注入机制：**
- 扫描实现 `MsgController` 接口的实体类
- 查找标记 `EFRpcAutowired` 属性的字段
- 根据属性类型从 IOC 容器中获取实例并注入

### 3. 通信协议实现

#### 3.1 RabbitMQ RPC 模式

**客户端（生产者）实现：**

1. **队列命名规则**：`version + interfaceFullName + "." + methodName`
2. **请求流程**：
   - 创建临时回复队列
   - 设置 `CorrelationId` 和 `ReplyTo` 属性
   - 序列化请求参数为 `SuperMsgMulti`
   - 发送到目标队列
   - 监听临时队列等待响应

```csharp
// 创建临时队列
String replyQueue = channel.QueueDeclare().QueueName;
IBasicProperties properties = channel.CreateBasicProperties();
properties.ReplyTo = replyQueue;
properties.CorrelationId = correlationId;

// 监听回调
callbackConsumer.Received += (model, ea) =>
{
    if (ea.BasicProperties.CorrelationId == correlationId)
    {
        SuperMsgMulti superMsg = this.serializer.DeSerializeBytes<SuperMsgMulti>(ea.Body.ToArray());
        mfs.setMsg(superMsg);
        mfs.release(superMsg.Id);
    }
};
```

**服务端（消费者）实现：**

1. **队列注册**：为每个方法创建对应的队列
2. **消息处理**：
   - 反序列化请求消息
   - 通过反射调用目标方法
   - 序列化返回值
   - 发送到回复队列

```csharp
channel.QueueDeclare(queue: this.version + this.interfaceFullName + "." + md.Name, 
    durable: false, exclusive: false, autoDelete: false, arguments: null);

consumer.Received += (model, ea) =>
{
    SuperMsgMulti superMsg = this.serializer.DeSerializeBytes<SuperMsgMulti>(ea.Body.ToArray());
    
    // 反射调用方法
    object[] objs = new object[superMsg.msg.Length];
    for (int j = 0; j < superMsg.msg.Length; j++)
    {
        objs[j] = this.serializer.DeSerializeString(mfs.reqs[j], superMsg.msg[j].ToString());
    }
    object rep = mfs.methodInfo.Invoke(this.ControllerObj, objs);
    
    // 发送响应
    if (md.ReturnType != typeof(void))
    {
        channel.BasicPublish(exchange: "", routingKey: properties.ReplyTo,
            basicProperties: replyProerties, 
            body: this.serializer.SerializeBytes(superMsg.setReq(rep)));
    }
};
```

#### 3.2 同步机制

**Synchronizer 设计：**
参考 Java J.U.C 中的 Synchronizer，实现类似 AQS 的同步机制：

```csharp
public abstract class Synchronizer
{
    protected Thread t;
    protected int sleeptime = 0;
    
    public void acquire()
    {
        if (tryAcquire()) {
            try
            {
                if (sleeptime > 0) { Thread.Sleep(sleeptime); }
                else { Thread.Sleep(Timeout.Infinite); }
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
}
```

**线程同步管理：**
- 使用 `ConcurrentDictionary<string, UnsafeSynchronizer>` 缓存请求 ID 与同步器的映射
- 通过线程中断机制实现同步挂起和释放

### 4. 序列化机制

**消息结构：**
```csharp
public class SuperMsgMulti : BaseMsg
{
    public object[] msg { get; private set; }  // 请求参数
    public object req { get; private set; }    // 响应结果
}
```

**序列化实现：**
- 使用 Newtonsoft.Json 进行序列化
- 支持字节数组和字符串两种格式
- 提供泛型反序列化支持

### 5. 使用流程

#### 5.1 服务端开发
```csharp
[EFRpcService(version = "v1.")]
public class MsgServiceImpl : IMsgServer, MsgController
{
    public GetMsgSumReply GetSum(GetMsgNumRequest request)
    {
        var result = new GetMsgSumReply();
        result.Sum = request.Num1 + request.Num2;
        return result;
    }
}

// 启动服务
new ConsumerBootstrap<Program>().start(new RabbitMQOptionsFactory<RabbitMQMsgConsumerMap>());
```

#### 5.2 客户端开发
```csharp
public class MsgClientImpl : IMsgClient, MsgController
{
    [EFRpcAutowired(version = "v1", runMode = "Auto")]
    public IMsgServer ServiceAutoUpdate;
    
    public int GetSum(int num1, int num2)
    {
        return ServiceAutoUpdate.GetSum(new GetMsgNumRequest() { Num1 = num1, Num2 = num2 }).Sum;
    }
}

// 启动客户端
MsgClientImpl msgClientImpl = new ProducerBootstrap<Program>()
    .start(new RabbitMQOptionsFactory<RabbitMQMsgProducerMap>())
    .getController<MsgClientImpl>();
```

## 技术特点

### 1. 优势
- **注解驱动**：简化配置，提高开发效率
- **动态代理**：透明化远程调用
- **协议无关**：支持多种通信协议
- **轻量级**：依赖少，易于集成
- **异步支持**：支持同步和异步调用模式

### 2. 局限性
- **功能相对简单**：相比 Dubbo、gRPC 等成熟框架功能较少
- **性能优化空间**：序列化、网络传输等方面有优化空间
- **生态不完善**：缺少监控、治理等企业级功能

## 总结

MQTT-RPC.NET 是一个设计简洁的 RPC 框架，通过注解驱动和动态代理技术实现了透明的远程服务调用。其核心价值在于：

1. **学习价值**：展示了 RPC 框架的基本实现原理
2. **参考价值**：为类似项目提供了架构设计参考
3. **实用价值**：适合轻量级分布式系统使用

该框架虽然功能相对简单，但架构设计清晰，代码结构良好，是一个很好的 RPC 框架学习案例。 