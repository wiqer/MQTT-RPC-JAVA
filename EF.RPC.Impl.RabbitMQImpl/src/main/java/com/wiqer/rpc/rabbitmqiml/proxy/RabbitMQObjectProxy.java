package com.wiqer.rpc.rabbitmqiml.proxy;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.wiqer.rpc.impl.annotation.EFRpcMethod;
import com.wiqer.rpc.impl.core.BaseMsgFun;
import com.wiqer.rpc.impl.proxy.ObjectProxy;
import com.wiqer.rpc.impl.util.ServiceUtil;
import com.wiqer.rpc.rabbitmqiml.RabbitMQMsgFun;
import com.wiqer.rpc.rabbitmqiml.RabbitMQRpcClient;
import com.wiqer.rpc.serialize.JsonSerializer;
import com.wiqer.rpc.serialize.SerializerInterface;
import com.wiqer.rpc.serialize.SuperMsgMulti;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class RabbitMQObjectProxy extends ObjectProxy {
    ConcurrentHashMap<Method, RabbitMQMsgFun> methodMsgFunMap=new ConcurrentHashMap<>();
   SerializerInterface serializer= new JsonSerializer();
    public RabbitMQObjectProxy(Class clazz, String version, RabbitMQRpcClient rpcClient) {
        super(clazz, version);
        Arrays.stream(clazz.getMethods()).forEach(method -> {

            //方法名
            String methodName = method.getName();

            //参数集合
            if (methodName.equals("toString") || methodName.equals("equals") || methodName.equals("hashCode") ||
                    methodName.equals("getClass")|| methodName.equals("notify") || methodName.equals("notifyAll")
                    || methodName.equals("wait") ) return;
            String serviceName = ServiceUtil.makeServiceKey(methodName, version);
            String queName= serviceName + "." + method.getName();
            EFRpcMethod[] efRpcMethod=method.getAnnotationsByType(EFRpcMethod.class);
            if(efRpcMethod!=null||efRpcMethod.length>0){
                queName+=efRpcMethod[0].mark();
            }
            
            BaseMsgFun baseMsgFun =(BaseMsgFun)this.get(queName);
            RabbitMQMsgFun rabbitMQMsgFun =new RabbitMQMsgFun();
            BeanUtils.copyProperties(baseMsgFun,rabbitMQMsgFun);
            if (baseMsgFun.getResponseType() !=Void.class)
            {

                try {
                    Channel channel= rpcClient.getConnection().createChannel();
                    baseMsgFun.initUnsafeSynchronizer();
                    String correlationId = baseMsgFun.ReqFullName;
                    // 创建一个临时队列, 返回队列的名字
                    final String corrId = UUID.randomUUID().toString();

                    String replyQueueName = channel.queueDeclare().getQueue();
                    AMQP.BasicProperties props = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(corrId)
                            .replyTo(replyQueueName)
                            .build();
                    rabbitMQMsgFun.setProperties(props);
                    rabbitMQMsgFun.setChannel(channel);
                    this.put(queName,rabbitMQMsgFun);
                    String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
                        if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                            SuperMsgMulti superMsg = this.serializer.DeSerializeString(
                                    new String(delivery.getBody(), "UTF-8"),
                                    SuperMsgMulti.class);
                            baseMsgFun.setMsg(superMsg);
                            baseMsgFun.release(superMsg.Id);
                        }
                    }, consumerTag -> {
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }




    @Override
    protected boolean sendMsg(Object proxy, Method method, SuperMsgMulti superMsgMulti,String markName) throws IOException {
        Channel channel = null;
        RabbitMQMsgFun msgFun;
        if((msgFun=methodMsgFunMap.get(method))==null){
            String serviceName = ServiceUtil.makeServiceKey(method.getName(), version);
            AtomicReference<String> queName= new AtomicReference<>(serviceName + "." + method.getName());
            Arrays.stream(method.getParameterTypes()).forEach(classType->{
                queName.set(queName.get()+classType.toString().hashCode()%100+"");
            });
            msgFun =(RabbitMQMsgFun)this.get(queName.get());
            methodMsgFunMap.put(method,msgFun);
            channel=msgFun.getChannel();

        }
       String body =  this.serializer.SerializeString(superMsgMulti);
        channel.basicPublish("", msgFun.FullName, msgFun.getProperties(), body.getBytes("UTF-8"));
        return false;
    }
}
