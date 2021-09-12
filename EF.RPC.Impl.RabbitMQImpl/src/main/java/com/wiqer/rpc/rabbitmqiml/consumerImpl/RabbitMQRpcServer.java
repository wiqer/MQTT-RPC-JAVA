package com.wiqer.rpc.rabbitmqiml.consumerImpl;

import com.rabbitmq.client.*;
import com.wiqer.rpc.impl.RpcServer;
import com.wiqer.rpc.impl.annotation.EFRpcMethod;
import com.wiqer.rpc.impl.core.RpcServerHandler;
import com.wiqer.rpc.impl.core.ServerHandler;
import com.wiqer.rpc.serialize.JsonSerializer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class RabbitMQRpcServer extends RpcServer {
    public RabbitMQRpcServer(String serverAddress) {
        super(serverAddress);
    }
    ServerHandler serverHandler =new RpcServerHandler(new JsonSerializer());
    public RabbitMQRpcServer(String serverAddress, ServerHandler serverHandler) {
        super(serverAddress);
        this.serverHandler=serverHandler;
    }
    @Override
    public void serverRun() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(serverAddress);
//        threadPoolExecutor.execute();
        serviceMap.forEach((serviceName,serviceBean)->{
            Class<?> serviceClass = serviceBean.getClass();
            Method[] methods= serviceClass.getMethods();
            Arrays.stream(methods).forEach(method->{
                //方法名
                String methodName = method.getName();
                //参数集合
                if (methodName.equals("toString") || methodName.equals("equals") || methodName.equals("hashCode") ||
                        methodName.equals("getClass")|| methodName.equals("notify") || methodName.equals("notifyAll")
                        || methodName.equals("wait") ) return;
                //防止方法重复
                String queName=serviceName + "." + method.getName();
                EFRpcMethod[] efRpcMethod=method.getAnnotationsByType(EFRpcMethod.class);
                if(efRpcMethod!=null||efRpcMethod.length>0){
                    queName+=efRpcMethod[0].mark();
                }
//                   Arrays.stream(method.getParameterTypes()).forEach(classType->{
//                       queName.set(queName.get()+classType.toString().hashCode()%100+"");
//                    });
                    try (Connection connection = factory.newConnection();
                         Channel channel = connection.createChannel()) {
                        channel.queueDeclare(queName, false, false, false, null);
                        channel.queuePurge(queName);
                        System.out.println("注册队列：(" + queName+ ")");
                        channel.basicQos(1);
                        System.out.println(" [x] Awaiting RPC requests");
                        Object monitor = new Object();
                        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                            String result=null;
                            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                                    .Builder()
                                    .correlationId(delivery.getProperties().getCorrelationId())
                                    .build();
                            try {
                                String message = new String(delivery.getBody(), "UTF-8");
                                int n = Integer.parseInt(message);
                                System.out.println("获取消息：(" + message + ")");
                                //do invoke
                                 result=  serverHandler.handle(message,serviceBean,method);
                            } catch (RuntimeException e) {
                                System.out.println(" 服务出现异常： " + e.toString());
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            } finally {
                                if(null!=result) {
                                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, result.getBytes("UTF-8"));
                                }
                                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                                // RabbitMq consumer worker thread notifies the RPC server owner thread
                                synchronized (monitor) {
                                    monitor.notify();
                                }
                            }
                        };

                        channel.basicConsume(queName, false, deliverCallback, (consumerTag -> {
                        }));
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            });


    }
}
