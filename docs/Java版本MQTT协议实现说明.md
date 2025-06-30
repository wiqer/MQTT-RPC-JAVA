# Java版本MQTT协议实现说明

## 概述

本文档描述了EFRPCJAVA项目中MQTT协议版本的实现，该实现与.NET版本保持相同的协议、服务发现和RPC机制。

## 架构设计

### 1. 整体架构

MQTT版本的RPC框架采用与.NET版本相同的分层架构：

```
┌─────────────────┐
│   注解层        │  @EFRpcService, @EFRpcAutowired
├─────────────────┤
│   代理层        │  动态代理实现
├─────────────────┤
│   通信层        │  MQTT协议实现
├─────────────────┤
│   序列化层      │  JSON序列化
└─────────────────┘
```

### 2. 核心组件

#### 2.1 MQTT配置类

**MQTTOptions**
```java
public class MQTTOptions extends Options {
    private String brokerUrl = "tcp://localhost:1883";
    private String clientId;
    private String username;
    private String password;
    private int keepAliveInterval = 60;
    private int connectionTimeout = 30;
    private boolean cleanSession = true;
    private int maxInflight = 1000;
    private int qos = 1;
}
```

**MQTTOptionsFactory**
```java
public class MQTTOptionsFactory<MsgMathsInfoMap> extends MsgMathsInfoFactory<MsgMathsInfoMap> {
    private MQTTOptions options;
    
    public MQTTOptionsFactory() {
        this.options = new MQTTOptions();
        // 默认配置
    }
}
```

#### 2.2 MQTT消息结构

**MQTTMessage**
```java
public class MQTTMessage extends BaseMsg {
    private Object[] msg;  // 请求参数
    private Object req;    // 响应结果
    private String correlationId; // 关联ID
    private String replyTo; // 回复主题
    
    public MQTTMessage setResponse(Object response) {
        this.req = response;
        return this;
    }
}
```

#### 2.3 生产者实现

**MQTTMsgProducerMap**
- 负责客户端RPC调用
- 创建MQTT客户端连接
- 发布请求消息到指定主题
- 订阅回复主题等待响应
- 使用同步器实现同步调用

**核心功能：**
1. **连接管理**：创建和维护MQTT客户端连接
2. **消息发布**：将RPC请求发布到`efrpc/request/{service}.{method}`主题
3. **响应处理**：订阅`efrpc/reply/{correlationId}`主题接收响应
4. **同步机制**：使用`UnsafeSynchronizer`实现同步等待

#### 2.4 消费者实现

**MQTTMsgConsumerMap**
- 负责服务端RPC处理
- 创建MQTT客户端连接
- 订阅请求主题处理RPC调用
- 发布响应消息到回复主题

**核心功能：**
1. **服务注册**：订阅`efrpc/request/{service}.{method}`主题
2. **请求处理**：反序列化请求参数，反射调用目标方法
3. **响应发送**：将方法返回值发布到回复主题
4. **错误处理**：处理连接异常和方法调用异常

## 协议设计

### 1. 主题命名规范

**请求主题：**
```
efrpc/request/{clientId}.{className}.{methodName}
```

**回复主题：**
```
efrpc/reply/{correlationId}
```

### 2. 消息格式

**请求消息：**
```json
{
  "id": "unique-message-id",
  "createDate": "2024-01-01T00:00:00Z",
  "msg": [param1, param2, ...],
  "correlationId": "unique-correlation-id",
  "replyTo": "efrpc/reply/unique-correlation-id"
}
```

**响应消息：**
```json
{
  "id": "unique-message-id",
  "createDate": "2024-01-01T00:00:00Z",
  "req": "response-value",
  "correlationId": "unique-correlation-id",
  "replyTo": "efrpc/reply/unique-correlation-id"
}
```

### 3. 通信流程

```
客户端                                   服务端
  │                                        │
  │ 1. 连接MQTT Broker                      │
  │                                        │
  │ 2. 订阅回复主题                         │
  │                                        │
  │ 3. 发布请求消息                         │
  │ ──────────────────────────────────────→ │
  │                                        │ 4. 接收请求消息
  │                                        │
  │                                        │ 5. 反序列化参数
  │                                        │
  │                                        │ 6. 反射调用方法
  │                                        │
  │                                        │ 7. 序列化返回值
  │                                        │
  │ 8. 接收响应消息                         │
  │ ←────────────────────────────────────── │
  │                                        │
  │ 9. 处理响应结果                         │
  │                                        │
```

## 使用示例

### 1. 服务端开发

**服务实现类：**
```java
@EFRpcService(version = "v1", strategyType = MQTTMsgServiceImpl.class)
public class MQTTMsgServiceImpl implements IMsgServer, MsgController {
    
    @Override
    public GetMsgSumReply GetSum(GetMsgNumRequest request) {
        GetMsgSumReply result = new GetMsgSumReply();
        result.setSum(request.getNum1() + request.getNum2());
        System.out.println("MQTT服务端: 计算 " + request.getNum1() + " + " + request.getNum2() + " = " + result.getSum());
        return result;
    }
    
    @Override
    public int GetNum(int num1, int num2) {
        int result = num1 + num2;
        System.out.println("MQTT服务端: 计算 " + num1 + " + " + num2 + " = " + result);
        return result;
    }
    
    @Override
    public String SendMessage(String message) {
        String result = "MQTT服务端收到消息: " + message;
        System.out.println(result);
        return result;
    }
}
```

**启动服务：**
```java
// 配置MQTT选项
MQTTOptions options = new MQTTOptions();
options.setBrokerUrl("tcp://localhost:1883");
options.setClientId("EFRPC_Server_" + System.currentTimeMillis());

// 创建消费者映射
MQTTMsgConsumerMap consumerMap = new MQTTMsgConsumerMap();
consumerMap.setOptions(options);

// 注册服务
consumerMap.GetMathsInfo(MQTTMsgServiceImpl.class);
```

### 2. 客户端开发

**客户端实现类：**
```java
public class MQTTMsgClientImpl implements IMsgClient {
    
    @EFRpcAutowired(version = "v1")
    public IMsgServer ServiceAutoUpdate;
    
    @Override
    public int GetSum(int num1, int num2) {
        return ServiceAutoUpdate.GetSum(new GetMsgNumRequest(num1, num2)).getSum();
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

**启动客户端：**
```java
// 配置MQTT选项
MQTTOptions options = new MQTTOptions();
options.setBrokerUrl("tcp://localhost:1883");
options.setClientId("EFRPC_Client_" + System.currentTimeMillis());

// 创建生产者映射
MQTTMsgProducerMap producerMap = new MQTTMsgProducerMap();
producerMap.setOptions(options);

// 注册客户端
producerMap.GetMathsInfo(MQTTMsgClientImpl.class);

// 创建客户端实例并调用
MQTTMsgClientImpl client = new MQTTMsgClientImpl();
int sum = client.GetSum(10, 20);
System.out.println("GetSum(10, 20) = " + sum);
```

### 3. 完整测试示例

```java
public class MQTTTest {
    
    public static void main(String[] args) {
        try {
            // 配置MQTT选项
            MQTTOptions options = new MQTTOptions();
            options.setBrokerUrl("tcp://localhost:1883");
            options.setClientId("EFRPC_Test_" + System.currentTimeMillis());
            
            // 启动服务端
            startServer(options);
            
            // 等待服务端启动
            Thread.sleep(2000);
            
            // 启动客户端
            startClient(options);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void startServer(MQTTOptions options) {
        new Thread(() -> {
            try {
                System.out.println("启动MQTT服务端...");
                
                MQTTMsgConsumerMap consumerMap = new MQTTMsgConsumerMap();
                consumerMap.setOptions(options);
                consumerMap.GetMathsInfo(MQTTMsgServiceImpl.class);
                
                System.out.println("MQTT服务端启动成功，等待客户端连接...");
                
                while (true) {
                    Thread.sleep(1000);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private static void startClient(MQTTOptions options) {
        new Thread(() -> {
            try {
                System.out.println("启动MQTT客户端...");
                
                MQTTMsgProducerMap producerMap = new MQTTMsgProducerMap();
                producerMap.setOptions(options);
                producerMap.GetMathsInfo(MQTTMsgClientImpl.class);
                
                MQTTMsgClientImpl client = new MQTTMsgClientImpl();
                
                // 测试RPC调用
                System.out.println("开始测试MQTT RPC调用...");
                
                int sum = client.GetSum(10, 20);
                System.out.println("GetSum(10, 20) = " + sum);
                
                int num = client.GetNum(5, 15);
                System.out.println("GetNum(5, 15) = " + num);
                
                String response = client.SendMessage("Hello MQTT RPC!");
                System.out.println("SendMessage响应: " + response);
                
                System.out.println("MQTT RPC测试完成！");
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
```

## 技术特点

### 1. 优势

- **协议标准化**：使用MQTT标准协议，支持多种MQTT Broker
- **轻量级**：MQTT协议轻量，适合物联网和移动设备
- **可靠性**：支持QoS级别，确保消息可靠传递
- **扩展性**：基于主题的发布订阅模式，易于扩展
- **跨平台**：与.NET版本保持相同的协议和接口

### 2. 与.NET版本的一致性

- **相同的注解系统**：`@EFRpcService`和`@EFRpcAutowired`
- **相同的消息结构**：`MQTTMessage`对应`SuperMsgMulti`
- **相同的配置方式**：`MQTTOptions`对应`RabbitMQOptions`
- **相同的启动流程**：消费者和生产者启动方式一致

### 3. 配置选项

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

## 部署要求

### 1. MQTT Broker

推荐使用以下MQTT Broker之一：
- **Mosquitto**：轻量级开源MQTT Broker
- **Eclipse HiveMQ**：企业级MQTT Broker
- **EMQ X**：高性能分布式MQTT Broker
- **RabbitMQ**：支持MQTT插件

### 2. 依赖库

```xml
<dependency>
    <groupId>org.eclipse.paho</groupId>
    <artifactId>org.eclipse.paho.client.mqttv3</artifactId>
    <version>1.2.5</version>
</dependency>
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.75</version>
</dependency>
```

### 3. 网络要求

- MQTT Broker需要可访问
- 默认端口1883（非SSL）或8883（SSL）
- 支持TCP连接

## 总结

Java版本的MQTT协议实现完全保持了与.NET版本相同的架构设计和接口规范，通过MQTT协议实现了轻量级、可靠的RPC通信。该实现特别适合物联网场景和需要跨平台兼容的应用场景。 