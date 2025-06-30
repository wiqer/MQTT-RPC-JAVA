package com.wiqer.rpc.client;

/**
 * MQTT消息客户端实现
 * 对应.NET版本的MsgClientImpl
 */
public class MQTTMsgClientImpl implements IMsgClient {
    
    // @EFRpcAutowired(version = "v1")
    public IMsgServer ServiceAutoUpdate;
    
    @Override
    public int GetSum(int num1, int num2) {
        return ServiceAutoUpdate.GetSum(new GetMsgNumRequest(num1, num2)).getSum();
    }
    
    @Override
    public int GetNum(int num1, int num2) {
        return ServiceAutoUpdate.GetNum(num1, num2);
    }
    
    @Override
    public String SendMessage(String message) {
        return ServiceAutoUpdate.SendMessage(message);
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