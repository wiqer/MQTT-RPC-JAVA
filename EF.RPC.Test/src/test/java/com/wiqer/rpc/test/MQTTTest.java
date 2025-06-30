package com.wiqer.rpc.test;

import com.wiqer.rpc.impl.mqttimpl.MQTTOptions;
import com.wiqer.rpc.impl.mqttimpl.MQTTOptionsFactory;
import com.wiqer.rpc.impl.mqttimpl.consumerimpl.MQTTMsgConsumerMap;
import com.wiqer.rpc.impl.mqttimpl.producerimpl.MQTTMsgProducerMap;
import com.wiqer.rpc.app.MQTTMsgServiceImpl;
import com.wiqer.rpc.client.MQTTMsgClientImpl;

/**
 * MQTT RPC测试类
 * 演示MQTT协议版本的RPC调用
 */
public class MQTTTest {
    
    public static void main(String[] args) {
        try {
            // 配置MQTT选项
            MQTTOptions options = new MQTTOptions();
            options.setBrokerUrl("tcp://localhost:1883");
            options.setClientId("EFRPC_Test_" + System.currentTimeMillis());
            options.setKeepAliveInterval(60);
            options.setConnectionTimeout(30);
            options.setCleanSession(true);
            options.setQos(1);
            
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
                
                // 创建消费者映射
                MQTTMsgConsumerMap consumerMap = new MQTTMsgConsumerMap();
                consumerMap.setOptions(options);
                
                // 注册服务
                consumerMap.GetMathsInfo(MQTTMsgServiceImpl.class);
                
                System.out.println("MQTT服务端启动成功，等待客户端连接...");
                
                // 保持服务运行
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
                
                // 创建生产者映射
                MQTTMsgProducerMap producerMap = new MQTTMsgProducerMap();
                producerMap.setOptions(options);
                
                // 注册客户端
                producerMap.GetMathsInfo(MQTTMsgClientImpl.class);
                
                // 创建客户端实例
                MQTTMsgClientImpl client = new MQTTMsgClientImpl();
                
                // 测试RPC调用
                System.out.println("开始测试MQTT RPC调用...");
                
                // 测试GetSum方法
                int sum = client.GetSum(10, 20);
                System.out.println("GetSum(10, 20) = " + sum);
                
                // 测试GetNum方法
                int num = client.GetNum(5, 15);
                System.out.println("GetNum(5, 15) = " + num);
                
                // 测试SendMessage方法
                String response = client.SendMessage("Hello MQTT RPC!");
                System.out.println("SendMessage响应: " + response);
                
                System.out.println("MQTT RPC测试完成！");
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
} 