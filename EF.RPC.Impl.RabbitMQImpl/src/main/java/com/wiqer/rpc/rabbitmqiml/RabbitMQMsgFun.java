package com.wiqer.rpc.rabbitmqiml;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.wiqer.rpc.impl.core.BaseMsgFun;

public class RabbitMQMsgFun extends BaseMsgFun {
    Channel channel;
    AMQP.BasicProperties properties;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public AMQP.BasicProperties getProperties() {
        return properties;
    }

    public void setProperties(AMQP.BasicProperties properties) {
        this.properties = properties;
    }
}
