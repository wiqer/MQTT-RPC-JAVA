package com.wiqer.rpc.impl.mqttimpl.consumerimpl;

import com.alibaba.fastjson.JSON;
import com.wiqer.rpc.impl.MsgMathsInfo;
import com.wiqer.rpc.impl.consumerImpl.MsgConsumerMap;
import com.wiqer.rpc.impl.mqttimpl.MQTTOptions;
import com.wiqer.rpc.impl.mqttimpl.MQTTMessage;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MQTT消息消费者映射
 * 对应.NET版本的RabbitMQMsgConsumerMap
 */
public class MQTTMsgConsumerMap extends MsgConsumerMap {
    
    private static final Logger logger = LoggerFactory.getLogger(MQTTMsgConsumerMap.class);
    
    private MQTTOptions options;
    private MqttClient mqttClient;
    
    public MQTTMsgConsumerMap setOptions(MQTTOptions options) {
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
                    handleRequest(topic, message);
                }
                
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 消息发送完成
                }
            });
            
            // 创建服务实例
            this.ControllerObj = clazz.newInstance();
            this.packageName = clazz.getPackage().getName();
            this.FullName = clazz.getName();
            this.className = clazz.getSimpleName();
            
            // 获取接口信息
            List<Class<?>> interfaces = Arrays.stream(clazz.getInterfaces())
                    .filter(iface -> !iface.getName().contains("MsgController"))
                    .collect(Collectors.toList());
            
            if (interfaces.size() != 1) {
                throw new RuntimeException("MsgController的实现类必须继承Sharing中共享的接口," +
                        "且不能继承MsgController外的其他接口,且不能多层继承");
            }
            
            this.interfaceFullName = interfaces.get(0).getName();
            
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
                
                // 订阅请求主题
                String requestTopic = "efrpc/request/" + mfs.getReqFullName();
                mqttClient.subscribe(requestTopic, options.getQos());
                
                this.put(method.getName(), mfs);
                
                logger.info("注册服务: 类名={}, 方法名={}, 返回值={}, 主题={}", 
                        clazz.getSimpleName(), method.getName(), method.getReturnType(), requestTopic);
            }
            
        } catch (Exception e) {
            logger.error("初始化MQTT消费者失败", e);
            throw new RuntimeException(e);
        }
    }
    
    private void handleRequest(String topic, MqttMessage message) {
        try {
            String messageStr = new String(message.getPayload());
            MQTTMessage request = JSON.parseObject(messageStr, MQTTMessage.class);
            
            // 从主题中提取方法名
            String methodName = topic.substring(topic.lastIndexOf("/") + 1);
            MsgMathsInfo mfs = this.get(methodName);
            
            if (mfs == null) {
                logger.error("未找到方法: {}", methodName);
                return;
            }
            
            // 反序列化参数
            Object[] args = new Object[request.getMsg().length];
            for (int i = 0; i < request.getMsg().length; i++) {
                args[i] = JSON.parseObject(JSON.toJSONString(request.getMsg()[i]), mfs.getReqs()[i]);
            }
            
            // 调用方法
            Object result = mfs.getMethodInfo().invoke(this.ControllerObj, args);
            
            // 发送响应（如果有返回值）
            if (mfs.getMethodInfo().getReturnType() != void.class) {
                MQTTMessage response = new MQTTMessage();
                response.setCorrelationId(request.getCorrelationId());
                response.setReplyTo(request.getReplyTo());
                response.setResponse(result);
                
                String responseJson = JSON.toJSONString(response);
                MqttMessage responseMessage = new MqttMessage(responseJson.getBytes());
                responseMessage.setQos(options.getQos());
                
                mqttClient.publish(request.getReplyTo(), responseMessage);
            }
            
        } catch (Exception e) {
            logger.error("处理MQTT请求失败", e);
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