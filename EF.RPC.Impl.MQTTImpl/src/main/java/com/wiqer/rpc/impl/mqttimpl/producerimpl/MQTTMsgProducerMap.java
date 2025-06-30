package com.wiqer.rpc.impl.mqttimpl.producerimpl;

import com.alibaba.fastjson.JSON;
import com.wiqer.rpc.impl.MsgMathsInfo;
import com.wiqer.rpc.impl.producerImpl.MsgProducerMap;
import com.wiqer.rpc.impl.sync.UnsafeSynchronizer;
import com.wiqer.rpc.impl.mqttimpl.MQTTOptions;
import com.wiqer.rpc.impl.mqttimpl.MQTTMessage;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * MQTT消息生产者映射
 * 对应.NET版本的RabbitMQMsgProducerMap
 */
public class MQTTMsgProducerMap extends MsgProducerMap {
    
    private static final Logger logger = LoggerFactory.getLogger(MQTTMsgProducerMap.class);
    
    private MQTTOptions options;
    private MqttClient mqttClient;
    private ConcurrentMap<String, UnsafeSynchronizer> synchronizerMap = new ConcurrentHashMap<>();
    
    public MQTTMsgProducerMap setOptions(MQTTOptions options) {
        this.options = options;
        return this;
    }
    
    @Override
    public void GetMathsInfo(Class<?> clazz) {
        this.clear();
        
        try {
            // 创建MQTT客户端
            mqttClient = new MqttClient(options.getBrokerUrl(), options.getClientId(), new MemoryPersistence());
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(options.isCleanSession());
            connectOptions.setConnectionTimeout(options.getConnectionTimeout());
            connectOptions.setKeepAliveInterval(options.getKeepAliveInterval());
            connectOptions.setMaxInflight(options.getMaxInflight());
            
            if (options.getUsername() != null) {
                connectOptions.setUserName(options.getUsername());
                connectOptions.setPassword(options.getPassword().toCharArray());
            }
            
            mqttClient.connect(connectOptions);
            
            // 设置消息回调
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    logger.error("MQTT连接丢失", cause);
                }
                
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    handleResponse(topic, message);
                }
                
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 消息发送完成
                }
            });
            
            // 处理类的方法
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getDeclaringClass() == Object.class) {
                    continue;
                }
                
                MsgMathsInfo mfs = new MsgMathsInfo();
                mfs.setName(method.getName());
                mfs.setMethodInfo(method);
                mfs.setReqFullName(options.getClientId() + "." + clazz.getSimpleName() + "." + method.getName());
                
                // 设置参数类型
                Parameter[] parameters = method.getParameters();
                Class<?>[] paramTypes = new Class<?>[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    paramTypes[i] = parameters[i].getType();
                }
                mfs.setReqs(paramTypes);
                
                // 初始化同步器（如果有返回值）
                if (method.getReturnType() != void.class) {
                    mfs.initUnsafeSynchronizer();
                }
                
                this.put(method.getName(), mfs);
            }
            
        } catch (Exception e) {
            logger.error("初始化MQTT生产者失败", e);
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        try {
            MsgMathsInfo mfs = this.get(method.getName());
            if (mfs == null) {
                throw new RuntimeException("方法 " + method.getName() + " 未找到");
            }
            
            // 创建请求消息
            MQTTMessage request = new MQTTMessage(args);
            request.setCorrelationId(UUID.randomUUID().toString());
            request.setReplyTo("efrpc/reply/" + request.getCorrelationId());
            
            // 序列化消息
            String messageJson = JSON.toJSONString(request);
            MqttMessage mqttMessage = new MqttMessage(messageJson.getBytes());
            mqttMessage.setQos(options.getQos());
            
            // 发布消息到请求主题
            String requestTopic = "efrpc/request/" + mfs.getReqFullName();
            mqttClient.publish(requestTopic, mqttMessage);
            
            // 订阅回复主题
            mqttClient.subscribe(request.getReplyTo(), options.getQos());
            
            // 等待响应（如果有返回值）
            if (method.getReturnType() != void.class) {
                UnsafeSynchronizer synchronizer = mfs.getUnsafeSynchronizer();
                synchronizerMap.put(request.getCorrelationId(), synchronizer);
                
                // 等待响应
                synchronizer.acquire();
                
                // 获取响应结果
                Object result = mfs.getMsg();
                synchronizerMap.remove(request.getCorrelationId());
                
                return result;
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("MQTT RPC调用失败", e);
            throw new RuntimeException(e);
        }
    }
    
    private void handleResponse(String topic, MqttMessage message) {
        try {
            String messageStr = new String(message.getPayload());
            MQTTMessage response = JSON.parseObject(messageStr, MQTTMessage.class);
            
            String correlationId = response.getCorrelationId();
            UnsafeSynchronizer synchronizer = synchronizerMap.get(correlationId);
            
            if (synchronizer != null) {
                // 设置响应结果
                MsgMathsInfo mfs = this.get(response.getId());
                if (mfs != null) {
                    mfs.setMsg(response.getReq());
                }
                
                // 释放同步器
                synchronizer.release();
            }
            
        } catch (Exception e) {
            logger.error("处理MQTT响应失败", e);
        }
    }
    
    public void close() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (Exception e) {
            logger.error("关闭MQTT客户端失败", e);
        }
    }
} 