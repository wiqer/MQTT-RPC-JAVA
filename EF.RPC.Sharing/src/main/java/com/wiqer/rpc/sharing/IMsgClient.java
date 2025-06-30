package com.wiqer.rpc.sharing;

/**
 * 消息客户端接口
 * 用于演示RPC客户端调用
 */
public interface IMsgClient {
    
    /**
     * 获取总和
     */
    int GetSum(int num1, int num2);
    
    /**
     * 获取数量
     */
    int GetNum(int num1, int num2);
    
    /**
     * 发送消息
     */
    String SendMessage(String message);
} 