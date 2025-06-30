package com.wiqer.rpc.impl.mqttimpl;

import com.wiqer.rpc.serialize.BaseMsg;

/**
 * MQTT RPC消息结构
 * 对应.NET版本的SuperMsgMulti
 */
public class MQTTMessage extends BaseMsg {
    
    private Object[] msg;  // 请求参数
    private Object req;    // 响应结果
    private String correlationId; // 关联ID
    private String replyTo; // 回复主题
    
    public MQTTMessage() {
        super();
    }
    
    public MQTTMessage(Object[] msg) {
        super();
        this.msg = msg;
    }
    
    public Object[] getMsg() {
        return msg;
    }
    
    public void setMsg(Object[] msg) {
        this.msg = msg;
    }
    
    public Object getReq() {
        return req;
    }
    
    public void setReq(Object req) {
        this.req = req;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }
    
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    
    public String getReplyTo() {
        return replyTo;
    }
    
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }
    
    /**
     * 设置响应结果并返回消息
     * 对应.NET版本的setReq方法
     */
    public MQTTMessage setResponse(Object response) {
        this.req = response;
        return this;
    }
} 