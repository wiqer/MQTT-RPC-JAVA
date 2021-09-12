package com.wiqer.rpc.nettyiml.consumerImpl;

import com.wiqer.rpc.impl.RpcServer;
import com.wiqer.rpc.impl.annotation.EFRpcMethod;
import com.wiqer.rpc.impl.core.RpcServerHandler;
import com.wiqer.rpc.impl.core.ServerHandler;
import com.wiqer.rpc.nettyiml.NettyMsg;
import com.wiqer.rpc.nettyiml.NettyMsgFun;
import com.wiqer.rpc.nettyiml.netty.NodeServer;
import com.wiqer.rpc.serialize.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class NettyRpcServer extends RpcServer {
    Map<String, NettyMsgFun> nettyMsgFunMap =new ConcurrentSkipListMap<>();
    BlockingQueue<NettyMsg> nettyMsgQueue=new LinkedBlockingQueue<>();
    ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(16);
    public NettyRpcServer(String serverAddress) {
        super(serverAddress);
    }
    ServerHandler serverHandler =new RpcServerHandler(new JsonSerializer());
    public NettyRpcServer(String serverAddress, ServerHandler serverHandler) {
        super(serverAddress);
        this.serverHandler=serverHandler;
    }
    @Override
    public void serverRun()  {

//        threadPoolExecutor.execute();
        serviceMap.forEach((serviceName,serviceBean)->{
            Class<?> serviceClass = serviceBean.getClass();
            Method[] methods= serviceClass.getMethods();
            Arrays.stream(methods).forEach(method->{
                //方法名
                String methodName = method.getName();
                //参数集合
                if (methodName.equals("toString") || methodName.equals("equals") || methodName.equals("hashCode") ||
                        methodName.equals("getClass")|| methodName.equals("notify") || methodName.equals("notifyAll")
                        || methodName.equals("wait") ) return;
                //防止方法重复
                String queName=serviceName + "." + method.getName();
                EFRpcMethod[] efRpcMethod=method.getAnnotationsByType(EFRpcMethod.class);
                if(efRpcMethod!=null||efRpcMethod.length>0){
                    queName+=efRpcMethod[0].mark();
                }
                NettyMsgFun msgFun =new NettyMsgFun();
                msgFun.setBean(serviceBean);
                msgFun.setMethod(method);
                nettyMsgFunMap.put(queName,msgFun);

            });
        });
        NodeServer nodeServer=new NodeServer();
        nodeServer.setNettyMsgQueue(nettyMsgQueue);
        try {
            nodeServer.startNettyServer(9182);
        }catch (Exception e){
            log.error("nodeServer  run",e);
        }
        while (true){
            try{

                NettyMsg nettyMsg=nettyMsgQueue.take();
                threadPoolExecutor.submit(()->{
                    NettyMsgFun nettyMsgFun=nettyMsgFunMap.get(nettyMsg.getQueName());
                    try {
                        serverHandler.handle(nettyMsg.getSuperMsgMulti(),nettyMsgFun.getBean(),nettyMsgFun.getMethod());
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });

            }catch (Exception e){
                log.error("BlockingQueue  take",e);
            }

        }

    }
}
