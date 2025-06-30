package com.wiqer.rpc.impl.mqttimpl;

import com.wiqer.rpc.impl.config.Options;

/**
 * MQTT配置选项
 * 对应.NET版本的RabbitMQOptions
 */
public class MQTTOptions extends Options {
    
    private String brokerUrl = "tcp://localhost:1883";
    private String clientId;
    private String username;
    private String password;
    private int keepAliveInterval = 60;
    private int connectionTimeout = 30;
    private boolean cleanSession = true;
    private int maxInflight = 1000;
    private int qos = 1;
    
    public MQTTOptions() {
        this.clientId = "EFRPC_" + System.currentTimeMillis();
    }
    
    public String getBrokerUrl() {
        return brokerUrl;
    }
    
    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }
    
    public void setKeepAliveInterval(int keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public boolean isCleanSession() {
        return cleanSession;
    }
    
    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }
    
    public int getMaxInflight() {
        return maxInflight;
    }
    
    public void setMaxInflight(int maxInflight) {
        this.maxInflight = maxInflight;
    }
    
    public int getQos() {
        return qos;
    }
    
    public void setQos(int qos) {
        this.qos = qos;
    }
} 