package com.wiqer.rpc.sharing;

/**
 * 消息服务接口
 * 用于演示RPC调用
 */
public interface IMsgServer {
    
    /**
     * 获取消息总和
     */
    GetMsgSumReply GetSum(GetMsgNumRequest request);
    
    /**
     * 获取消息数量
     */
    int GetNum(int num1, int num2);
    
    /**
     * 发送消息
     */
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