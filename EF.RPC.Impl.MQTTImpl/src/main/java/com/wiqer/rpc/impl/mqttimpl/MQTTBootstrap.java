package com.wiqer.rpc.impl.mqttimpl;

import com.wiqer.rpc.impl.bootstrap.ConsumerBootstrap;
import com.wiqer.rpc.impl.bootstrap.ProducerBootstrap;
import com.wiqer.rpc.impl.consumerImpl.MsgConsumerMap;
import com.wiqer.rpc.impl.producerImpl.MsgProducerMap;

/**
 * MQTT启动器
 * 对应.NET版本的ConsumerBootstrap和ProducerBootstrap
 */
public class MQTTBootstrap {
    
    /**
     * 消费者启动器
     */
    public static class Consumer<MsgConsumerMapType extends MsgConsumerMap> extends ConsumerBootstrap<MsgConsumerMapType> {
        
        private MQTTOptions options;
        
        public Consumer() {
            this.options = new MQTTOptions();
        }
        
        public Consumer(MQTTOptions options) {
            this.options = options;
        }
        
        public Consumer<MsgConsumerMapType> setOptions(MQTTOptions options) {
            this.options = options;
            return this;
        }
        
        @Override
        public void start() {
            try {
                // 这里可以添加MQTT特定的启动逻辑
                super.start();
            } catch (Exception e) {
                throw new RuntimeException("启动MQTT消费者失败", e);
            }
        }
    }
    
    /**
     * 生产者启动器
     */
    public static class Producer<MsgProducerMapType extends MsgProducerMap> extends ProducerBootstrap<MsgProducerMapType> {
        
        private MQTTOptions options;
        
        public Producer() {
            this.options = new MQTTOptions();
        }
        
        public Producer(MQTTOptions options) {
            this.options = options;
        }
        
        public Producer<MsgProducerMapType> setOptions(MQTTOptions options) {
            this.options = options;
            return this;
        }
        
        @Override
        public void start() {
            try {
                // 这里可以添加MQTT特定的启动逻辑
                super.start();
            } catch (Exception e) {
                throw new RuntimeException("启动MQTT生产者失败", e);
            }
        }
    }
} 