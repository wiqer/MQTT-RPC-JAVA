package com.wiqer.rpc.nettyiml.proxy;

import com.wiqer.rpc.impl.core.RpcException;
import com.wiqer.rpc.impl.core.SynchronizerManager;
import com.wiqer.rpc.impl.proxy.ObjectProxy;
import com.wiqer.rpc.nettyiml.netty.NettyClient;
import com.wiqer.rpc.serialize.JsonSerializer;
import com.wiqer.rpc.serialize.SerializerInterface;
import com.wiqer.rpc.serialize.SuperMsgMulti;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Netty对象代理 - 实现基于Netty的RPC调用
 */
@Slf4j
public class NettyObjectProxy extends ObjectProxy {

    private final NettyClient nettyClient;
    private final SynchronizerManager synchronizerManager;
    private final SerializerInterface serializer;

    public NettyObjectProxy(Class clazz, String version, NettyClient nettyClient) {
        super(clazz, version);
        this.nettyClient = nettyClient;
        this.synchronizerManager = new SynchronizerManager();
        this.serializer = new JsonSerializer();
    }

    @Override
    protected boolean sendMsg(Object proxy, Method method, SuperMsgMulti superMsgMulti, String markName) throws IOException {
        try {
            // 生成请求ID
            String requestId = UUID.randomUUID().toString();
            superMsgMulti.setId(requestId);
            
            // 创建同步器
            SynchronizerManager.UnsafeSynchronizer synchronizer = 
                synchronizerManager.createSynchronizer(requestId, 30000);
            
            // 序列化消息
            byte[] messageBytes = serializer.SerializeBytes(superMsgMulti);
            
            // 发送消息
            boolean sent = nettyClient.sendMessage(messageBytes);
            if (!sent) {
                throw new RpcException(RpcException.ErrorCodes.NETWORK_ERROR, 
                    "发送消息失败: " + method.getName());
            }
            
            // 等待响应
            if (method.getReturnType() != Void.class) {
                synchronizer.acquire();
                
                // 获取响应结果
                SuperMsgMulti responseMsg = synchronizerManager.getResponse(requestId);
                if (responseMsg != null) {
                    superMsgMulti.setResponse(responseMsg.getResponse());
                } else {
                    throw new RpcException(RpcException.ErrorCodes.TIMEOUT, 
                        "等待响应超时: " + method.getName());
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Netty RPC调用失败: {}", method.getName(), e);
            throw new RpcException(RpcException.ErrorCodes.INVOCATION_ERROR, 
                "RPC调用失败: " + method.getName(), e);
        }
    }
}
