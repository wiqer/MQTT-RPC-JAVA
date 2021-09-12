package com.wiqer.rpc.nettyiml.netty;

import com.wiqer.rpc.nettyiml.NettyMsg;
import com.wiqer.rpc.serialize.utils.FastJsonUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class NodeServerHandler  extends SimpleChannelInboundHandler<String> {
    Queue<NettyMsg> nettyMsgQueue ;

    public void setNettyMsgQueue(Queue<NettyMsg> nettyMsgQueue) {
        this.nettyMsgQueue = nettyMsgQueue;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) {
        if (StringUtils.isEmpty(message)) {
            return;
        }
        NettyMsg msg = FastJsonUtils.toBean(message, NettyMsg.class);
        nettyMsgQueue.offer(msg);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("some thing is error , " + cause.getMessage());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
        super.channelInactive(ctx);
    }

}
