package com.wiqer.rpc.rabbitmqiml.producerImpl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.wiqer.rpc.impl.proxy.ObjectProxy;
import com.wiqer.rpc.impl.sync.SynchronizerManager;
import com.wiqer.rpc.serialize.JsonSerializer;
import com.wiqer.rpc.serialize.SuperMsgMulti;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ对象代理 - 集成同步器管理
 * 参考.NET版本的实现，提供完整的RPC调用支持
 */
@Slf4j
public class RabbitMQObjectProxy extends ObjectProxy {
    
    private final RabbitMQRpcClient client;
    private final SynchronizerManager synchronizerManager;
    private final JsonSerializer serializer;
    
    public RabbitMQObjectProxy(Class clazz, String version, RabbitMQRpcClient client) {
        super(clazz, version);
        this.client = client;
        this.synchronizerManager = new SynchronizerManager();
        this.serializer = new JsonSerializer();
    }
    
    @Override
    protected boolean sendMsg(Object proxy, Method method, SuperMsgMulti superMsgMulti, String markName) throws IOException {
        String methodName = method.getName();
        String queueName = version + method.getDeclaringClass().getName() + "." + methodName;
        
        // 生成请求ID
        String requestId = UUID.randomUUID().toString();
        superMsgMulti.setId(requestId);
        
        // 创建同步器
        SynchronizerManager.UnsafeSynchronizer synchronizer = 
            synchronizerManager.createSynchronizer(requestId, 30000); // 30秒超时
        
        try {
            // 创建临时回复队列
            Channel channel = client.getConnection().createChannel();
            String replyQueueName = channel.queueDeclare().getQueue();
            
            // 设置消息属性
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(requestId)
                .replyTo(replyQueueName)
                .build();
            
            // 序列化请求
            String requestBody = serializer.SerializeString(superMsgMulti);
            
            // 发送请求
            channel.basicPublish("", queueName, props, requestBody.getBytes("UTF-8"));
            log.debug("发送RPC请求: {} -> {}", requestId, queueName);
            
            // 设置响应处理器
            channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(requestId)) {
                    try {
                        // 反序列化响应
                        SuperMsgMulti responseMsg = serializer.DeSerializeString(
                            SuperMsgMulti.class, new String(delivery.getBody(), "UTF-8"));
                        
                        // 存储响应结果
                        synchronizerManager.setResponse(requestId, responseMsg);
                        
                        // 释放同步器
                        synchronizerManager.releaseSynchronizer(requestId);
                        
                        log.debug("收到RPC响应: {}", requestId);
                    } catch (Exception e) {
                        log.error("处理RPC响应失败: {}", requestId, e);
                        synchronizerManager.releaseSynchronizer(requestId);
                    }
                }
            }, consumerTag -> {});
            
            // 等待响应
            synchronizer.acquire();
            
            // 获取响应结果
            SuperMsgMulti responseMsg = synchronizerManager.getResponse(requestId);
            if (responseMsg == null) {
                throw new RuntimeException("RPC调用超时: " + requestId);
            }
            
            // 设置响应结果
            superMsgMulti.setResponse(responseMsg.getResponse());
            
            return true;
            
        } catch (Exception e) {
            log.error("RPC调用失败: {}", requestId, e);
            throw new IOException("RPC调用失败", e);
        } finally {
            // 清理资源
            synchronizerManager.removeSynchronizer(requestId);
        }
    }
} 