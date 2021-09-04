package com.wiqer.rpc.rabbitmqiml;

import com.rabbitmq.client.*;
import com.wiqer.rpc.impl.RpcServer;
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
                //防止方法重复
                AtomicReference<String> queName= new AtomicReference<>(serviceName + "." + method.getName());
                   Arrays.stream(method.getParameterTypes()).forEach(classType->{
                       queName.set(queName.get()+classType.toString().hashCode()%100+"");
                    });
                    try (Connection connection = factory.newConnection();
                         Channel channel = connection.createChannel()) {
                        channel.queueDeclare(queName.get(), false, false, false, null);
                        channel.queuePurge(queName.get());
                        System.out.println("注册队列：(" + queName.get() + ")");
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

                        channel.basicConsume(queName.get(), false, deliverCallback, (consumerTag -> {
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
