package com.wiqer.rpc.websocketimpl;

import com.alibaba.fastjson.JSON;
import com.wiqer.rpc.impl.RpcServer;
import com.wiqer.rpc.impl.core.BaseServer;
import com.wiqer.rpc.impl.core.RpcServerHandler;
import com.wiqer.rpc.impl.core.ServerHandler;
import com.wiqer.rpc.serialize.JsonSerializer;
import com.wiqer.rpc.serialize.SuperMsgMulti;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket RPC服务器
 * 参考.NET版本的WebSocket实现
 */
@Slf4j
public class WebSocketRpcServer extends BaseServer {
    
    private final int port;
    private final ServerHandler serverHandler;
    private final Map<String, Method> methodMap = new ConcurrentHashMap<>();
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    
    public WebSocketRpcServer(String serverAddress) {
        super(serverAddress);
        this.port = Integer.parseInt(serverAddress.split(":")[1]);
        this.serverHandler = new RpcServerHandler(new JsonSerializer());
    }
    
    public WebSocketRpcServer(String serverAddress, ServerHandler serverHandler) {
        super(serverAddress);
        this.port = Integer.parseInt(serverAddress.split(":")[1]);
        this.serverHandler = serverHandler;
    }
    
    @Override
    public void serverRun() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            
                            // HTTP编解码器
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            
                            // WebSocket协议处理器
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true));
                            
                            // 自定义处理器
                            pipeline.addLast(new WebSocketRpcHandler());
                        }
                    });
            
            ChannelFuture future = bootstrap.bind(port).sync();
            log.info("WebSocket RPC服务器启动成功，监听端口: {}", port);
            
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("WebSocket RPC服务器启动失败", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    
    /**
     * 注册服务
     */
    public void registerService(String serviceName, Object serviceBean) {
        serviceMap.put(serviceName, serviceBean);
        
        // 扫描服务方法
        Method[] methods = serviceBean.getClass().getMethods();
        for (Method method : methods) {
            String methodKey = serviceName + "." + method.getName();
            methodMap.put(methodKey, method);
            log.info("注册WebSocket RPC方法: {}", methodKey);
        }
    }
    
    /**
     * WebSocket RPC处理器
     */
    private class WebSocketRpcHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
            if (frame instanceof TextWebSocketFrame) {
                String request = ((TextWebSocketFrame) frame).text();
                log.debug("收到WebSocket RPC请求: {}", request);
                
                try {
                    // 解析请求
                    WebSocketRpcRequest rpcRequest = JSON.parseObject(request, WebSocketRpcRequest.class);
                    
                    // 查找服务和方法
                    Object serviceBean = serviceMap.get(rpcRequest.getServiceName());
                    Method method = methodMap.get(rpcRequest.getServiceName() + "." + rpcRequest.getMethodName());
                    
                    if (serviceBean == null || method == null) {
                        sendErrorResponse(ctx, rpcRequest.getRequestId(), "服务或方法不存在");
                        return;
                    }
                    
                    // 构建SuperMsgMulti
                    SuperMsgMulti superMsg = new SuperMsgMulti();
                    superMsg.setId(rpcRequest.getRequestId());
                    superMsg.setMsg(rpcRequest.getParameters());
                    
                    // 调用服务
                    String response = serverHandler.handle(superMsg, serviceBean, method);
                    
                    // 发送响应
                    WebSocketRpcResponse rpcResponse = new WebSocketRpcResponse();
                    rpcResponse.setRequestId(rpcRequest.getRequestId());
                    rpcResponse.setResult(response);
                    rpcResponse.setSuccess(true);
                    
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(rpcResponse)));
                    
                } catch (Throwable e) {
                    log.error("处理WebSocket RPC请求失败", e);
                    sendErrorResponse(ctx, "unknown", e.getMessage());
                }
            }
        }
        
        private void sendErrorResponse(ChannelHandlerContext ctx, String requestId, String error) {
            WebSocketRpcResponse response = new WebSocketRpcResponse();
            response.setRequestId(requestId);
            response.setSuccess(false);
            response.setError(error);
            
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(response)));
        }
        
        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            log.info("WebSocket客户端连接: {}", ctx.channel().remoteAddress());
        }
        
        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            log.info("WebSocket客户端断开: {}", ctx.channel().remoteAddress());
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("WebSocket处理器异常", cause);
            ctx.close();
        }
    }
    
    /**
     * WebSocket RPC请求
     */
    public static class WebSocketRpcRequest {
        private String requestId;
        private String serviceName;
        private String methodName;
        private Object[] parameters;
        
        // getters and setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }
        
        public Object[] getParameters() { return parameters; }
        public void setParameters(Object[] parameters) { this.parameters = parameters; }
    }
    
    /**
     * WebSocket RPC响应
     */
    public static class WebSocketRpcResponse {
        private String requestId;
        private boolean success;
        private Object result;
        private String error;
        
        // getters and setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public Object getResult() { return result; }
        public void setResult(Object result) { this.result = result; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
} 