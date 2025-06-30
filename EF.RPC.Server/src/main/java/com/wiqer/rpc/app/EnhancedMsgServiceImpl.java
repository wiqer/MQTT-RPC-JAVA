package com.wiqer.rpc.app;

import com.wiqer.rpc.impl.annotation.EFRpcService;
import com.wiqer.rpc.impl.core.AbstractRpcService;
import lombok.extern.slf4j.Slf4j;

/**
 * 增强的消息服务实现
 * 改进设计：继承AbstractRpcService，实现统一的配置管理
 */
@Slf4j
@EFRpcService(version = "v1", serviceName = "EnhancedMsgService")
public class EnhancedMsgServiceImpl extends AbstractRpcService {
    
    public EnhancedMsgServiceImpl() {
        super();
        this.serviceName = "EnhancedMsgService";
        this.version = "v1";
    }
    
    @Override
    public void initialize() {
        log.info("初始化增强的消息服务: {}", this.serviceName);
    }
    
    @Override
    public void start() {
        log.info("启动增强的消息服务: {}", this.serviceName);
    }
    
    @Override
    public void stop() {
        log.info("停止增强的消息服务: {}", this.serviceName);
    }
    
    @Override
    public boolean isRunning() {
        return true; // 简化实现
    }
    
    /**
     * 获取总和
     */
    public int getSum(int num1, int num2) {
        log.info("处理getSum请求: num1={}, num2={}", num1, num2);
        
        int result = num1 + num2;
        log.info("getSum响应: result={}", result);
        return result;
    }
    
    /**
     * 获取数量
     */
    public int getNum(int num1, int num2) {
        log.info("处理getNum请求: num1={}, num2={}", num1, num2);
        
        int result = num1 + num2;
        log.info("getNum响应: result={}", result);
        return result;
    }
    
    /**
     * 发送消息
     */
    public String sendMessage(String message) {
        log.info("处理sendMessage请求: message={}", message);
        
        String response = "Enhanced Service Response: " + message;
        log.info("sendMessage响应: response={}", response);
        return response;
    }
} 