package com.wiqer.rpc.nettyiml.netty;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**

 */
@ChannelHandler.Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;

            if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                //向服务端发送消息
               //netty自带的事件传递
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        //断线了，可能只是client和server断了，但都和etcd没断。也可能是client自己断网了，也可能是server断了
        //发布断线事件。后续10秒后进行重连，根据etcd里的worker信息来决定是否重连，如果etcd里没了，就不重连。如果etcd里有，就重连
        notifyWorkerChange(ctx.channel());
    }

    private void notifyWorkerChange(Channel channel) {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String message) {

       //返回消息
    }

}
