package com.wiqer.rpc.app;

import com.wiqer.rpc.impl.annotation.EFRpcService;
import com.wiqer.rpc.impl.MsgController;
import com.wiqer.rpc.sharing.GetMsgNumRequest;
import com.wiqer.rpc.sharing.GetMsgSumReply;
import com.wiqer.rpc.sharing.IMsgServer;

/**
 * MQTT消息服务实现
 * 对应.NET版本的MsgServiceImpl
 */
@EFRpcService(version = "v1", strategyType = MQTTMsgServiceImpl.class)
public class MQTTMsgServiceImpl implements IMsgServer, MsgController {
    
    @Override
    public GetMsgSumReply GetSum(GetMsgNumRequest request) {
        GetMsgSumReply result = new GetMsgSumReply();
        result.setSum(request.getNum1() + request.getNum2());
        System.out.println("MQTT服务端: 计算 " + request.getNum1() + " + " + request.getNum2() + " = " + result.getSum());
        return result;
    }
    
    @Override
    public int GetNum(int num1, int num2) {
        int result = num1 + num2;
        System.out.println("MQTT服务端: 计算 " + num1 + " + " + num2 + " = " + result);
        return result;
    }
    
    @Override
    public String SendMessage(String message) {
        String result = "MQTT服务端收到消息: " + message;
        System.out.println(result);
        return result;
    }
} 