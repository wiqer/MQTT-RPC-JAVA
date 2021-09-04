package com.wiqer.rpc.rabbitmqiml;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.wiqer.rpc.impl.RpcClient;
import com.wiqer.rpc.impl.proxy.ObjectProxy;
import com.wiqer.rpc.rabbitmqiml.proxy.RabbitMQObjectProxy;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQRpcClient extends RpcClient {
    public Connection getConnection() {
        return connection;
    }

    private Connection connection;
    public RabbitMQRpcClient(String address) throws IOException, TimeoutException {
        super(address);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(address);

        connection = factory.newConnection();
    }

    @Override
    public <T, P> ObjectProxy createObjectProxy(Class<T> interfaceClass, String version) {
        ObjectProxy objectProxy=new RabbitMQObjectProxy(interfaceClass,version,this) ;
        return objectProxy;
    }
}
