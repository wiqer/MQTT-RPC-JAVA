package com.wiqer.rpc.client;

import com.wiqer.rpc.impl.annotation.EFRpcAutowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 消息客户端实现类
 * 演示客户端RPC调用
 */
@Slf4j
@Service
public class MsgClientImpl implements IMsgClient {
    
    @EFRpcAutowired(version = "v1.")
    public IMsgServer ServiceAutoUpdate;
    
    @Override
    public int GetSum(int num1, int num2) {
        log.info("客户端调用GetSum: num1={}, num2={}", num1, num2);
        
        GetMsgNumRequest request = new GetMsgNumRequest(num1, num2);
        GetMsgSumReply reply = ServiceAutoUpdate.GetSum(request);
        
        log.info("客户端GetSum结果: sum={}", reply.getSum());
        return reply.getSum();
    }
    
    @Override
    public int GetNum(int num1, int num2) {
        log.info("客户端调用GetNum: num1={}, num2={}", num1, num2);
        
        int result = ServiceAutoUpdate.GetNum(num1, num2);
        
        log.info("客户端GetNum结果: result={}", result);
        return result;
    }
    
    @Override
    public String SendMessage(String message) {
        log.info("客户端调用SendMessage: message={}", message);
        
        String response = ServiceAutoUpdate.SendMessage(message);
        
        log.info("客户端SendMessage结果: response={}", response);
        return response;
    }
}

/**
 * 消息客户端接口
 */
interface IMsgClient {
    int GetSum(int num1, int num2);
    int GetNum(int num1, int num2);
    String SendMessage(String message);
}

/**
 * 消息服务接口
 */
interface IMsgServer {
    GetMsgSumReply GetSum(GetMsgNumRequest request);
    int GetNum(int num1, int num2);
    String SendMessage(String message);
}

/**
 * 获取消息数量请求
 */
class GetMsgNumRequest {
    private int num1;
    private int num2;
    
    public GetMsgNumRequest() {}
    
    public GetMsgNumRequest(int num1, int num2) {
        this.num1 = num1;
        this.num2 = num2;
    }
    
    public int getNum1() {
        return num1;
    }
    
    public void setNum1(int num1) {
        this.num1 = num1;
    }
    
    public int getNum2() {
        return num2;
    }
    
    public void setNum2(int num2) {
        this.num2 = num2;
    }
}

/**
 * 获取消息总和响应
 */
class GetMsgSumReply {
    private int sum;
    
    public GetMsgSumReply() {}
    
    public GetMsgSumReply(int sum) {
        this.sum = sum;
    }
    
    public int getSum() {
        return sum;
    }
    
    public void setSum(int sum) {
        this.sum = sum;
    }
} 