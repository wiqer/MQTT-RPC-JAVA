package com.wiqer.rpc.rabbitmqiml;

import com.rabbitmq.client.ConnectionFactory;
import com.wiqer.rpc.impl.core.Options;

public class RabbitMQOptions implements Options {
    public ConnectionFactory factory;
}
