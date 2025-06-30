package com.wiqer.rpc.impl.mqttimpl;

import com.wiqer.rpc.impl.MsgMathsInfoFactory;

/**
 * MQTT配置工厂
 * 对应.NET版本的RabbitMQOptionsFactory
 */
public class MQTTOptionsFactory<MsgMathsInfoMap> extends MsgMathsInfoFactory<MsgMathsInfoMap> {
    
    private MQTTOptions options;
    
    public MQTTOptionsFactory() {
        this.options = new MQTTOptions();
        // 默认配置
        this.options.setBrokerUrl("tcp://localhost:1883");
        this.options.setClientId("EFRPC_" + System.currentTimeMillis());
        this.options.setKeepAliveInterval(60);
        this.options.setConnectionTimeout(30);
        this.options.setCleanSession(true);
        this.options.setMaxInflight(1000);
        this.options.setQos(1);
    }
    
    public MQTTOptionsFactory(MQTTOptions options) {
        this.options = options;
    }
    
    public MQTTOptions getOptions() {
        return options;
    }
    
    public void setOptions(MQTTOptions options) {
        this.options = options;
    }
    
    @Override
    public MsgMathsInfoMap getMsgMathsInfoMap() {
        return new MsgMathsInfoMap();
    }
} 