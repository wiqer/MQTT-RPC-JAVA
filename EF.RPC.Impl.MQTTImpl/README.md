# EF.RPC.Impl.MQTTImpl

MQTT协议版本的RPC实现模块，与.NET版本保持相同的协议、服务发现和RPC机制。

## 功能特性

- ✅ 基于MQTT协议的RPC通信
- ✅ 与.NET版本完全兼容的协议设计
- ✅ 支持同步和异步调用
- ✅ 自动服务发现和注册
- ✅ 注解驱动的开发模式
- ✅ 轻量级消息传输
- ✅ 支持QoS级别配置

## 核心组件

### 配置类
- `MQTTOptions` - MQTT连接配置
- `MQTTOptionsFactory` - 配置工厂类

### 消息结构
- `MQTTMessage` - MQTT RPC消息结构

### 生产者实现
- `MQTTMsgProducerMap` - 客户端RPC调用实现

### 消费者实现
- `MQTTMsgConsumerMap` - 服务端RPC处理实现

### 启动器
- `MQTTBootstrap` - MQTT启动器类

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.wiqer</groupId>
    <artifactId>EF.RPC.Impl.MQTTImpl</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 服务端实现

```java
@EFRpcService(version = "v1")
public class MyServiceImpl implements IMyService, MsgController {
    
    @Override
    public String hello(String name) {
        return "Hello " + name + " from MQTT RPC!";
    }
}
```

### 3. 客户端实现

```java
public class MyClientImpl implements IMyClient {
    
    @EFRpcAutowired(version = "v1")
    public IMyService service;
    
    @Override
    public String callHello(String name) {
        return service.hello(name);
    }
}
```

### 4. 启动服务

```java
// 配置MQTT选项
MQTTOptions options = new MQTTOptions();
options.setBrokerUrl("tcp://localhost:1883");

// 启动服务端
MQTTMsgConsumerMap consumerMap = new MQTTMsgConsumerMap();
consumerMap.setOptions(options);
consumerMap.GetMathsInfo(MyServiceImpl.class);

// 启动客户端
MQTTMsgProducerMap producerMap = new MQTTMsgProducerMap();
producerMap.setOptions(options);
producerMap.GetMathsInfo(MyClientImpl.class);
```

## 协议规范

### 主题命名
- 请求主题：`efrpc/request/{clientId}.{className}.{methodName}`
- 回复主题：`efrpc/reply/{correlationId}`

### 消息格式
```json
{
  "id": "unique-message-id",
  "createDate": "2024-01-01T00:00:00Z",
  "msg": [param1, param2, ...],
  "correlationId": "unique-correlation-id",
  "replyTo": "efrpc/reply/unique-correlation-id"
}
```

## 配置选项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| brokerUrl | tcp://localhost:1883 | MQTT Broker地址 |
| clientId | EFRPC_时间戳 | 客户端唯一标识 |
| username | null | 用户名 |
| password | null | 密码 |
| keepAliveInterval | 60 | 心跳间隔（秒） |
| connectionTimeout | 30 | 连接超时（秒） |
| cleanSession | true | 是否清理会话 |
| maxInflight | 1000 | 最大并发消息数 |
| qos | 1 | 服务质量级别 |

## 与.NET版本对比

| 特性 | Java版本 | .NET版本 |
|------|----------|----------|
| 协议 | MQTT | RabbitMQ |
| 注解 | @EFRpcService, @EFRpcAutowired | @EFRpcService, @EFRpcAutowired |
| 消息结构 | MQTTMessage | SuperMsgMulti |
| 配置类 | MQTTOptions | RabbitMQOptions |
| 序列化 | JSON | JSON |
| 同步机制 | UnsafeSynchronizer | Synchronizer |

## 依赖要求

- Java 8+
- MQTT Broker (如Mosquitto, HiveMQ, EMQ X)
- Eclipse Paho MQTT Client 1.2.5+
- FastJSON 1.2.75+

## 示例代码

完整的使用示例请参考：
- `docs/Java版本MQTT协议实现说明.md`
- `EF.RPC.Test/src/test/java/com/wiqer/rpc/test/MQTTTest.java`

## 许可证

本项目采用与主项目相同的许可证。 