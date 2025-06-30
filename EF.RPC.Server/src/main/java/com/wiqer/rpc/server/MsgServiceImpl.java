package com.wiqer.rpc.server;

import com.wiqer.rpc.impl.annotation.EFRpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 消息服务实现类
 * 演示服务端RPC服务
 */
@Slf4j
@Service
@EFRpcService(version = "v1.", strategyType = IMsgServer.class)
public class MsgServiceImpl implements IMsgServer {
    
    @Override
    public GetMsgSumReply GetSum(GetMsgNumRequest request) {
        log.info("收到GetSum请求: num1={}, num2={}", request.getNum1(), request.getNum2());
        
        GetMsgSumReply reply = new GetMsgSumReply();
        reply.setSum(request.getNum1() + request.getNum2());
        
        log.info("返回GetSum结果: sum={}", reply.getSum());
        return reply;
    }
    
    @Override
    public int GetNum(int num1, int num2) {
        log.info("收到GetNum请求: num1={}, num2={}", num1, num2);
        
        int result = num1 + num2;
        log.info("返回GetNum结果: result={}", result);
        return result;
    }
    
    @Override
    public String SendMessage(String message) {
        log.info("收到SendMessage请求: message={}", message);
        
        String response = "服务器收到消息: " + message;
        log.info("返回SendMessage结果: response={}", response);
        return response;
    }
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