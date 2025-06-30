package com.wiqer.rpc.nettyiml.netty;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * netty连接器
 *
 */
@Slf4j
public class NettyClient {
    private static final NettyClient nettyClient = new NettyClient();

    private Bootstrap bootstrap;

    private Map<String,Channel> channelMap =new ConcurrentSkipListMap<>();
    private List<Channel> channels=new ArrayList<>();
    public static NettyClient getInstance() {
        return nettyClient;
    }

    private NettyClient() {
        if (bootstrap == null) {
            bootstrap = initBootstrap();
        }
    }

    private Bootstrap initBootstrap() {
        //少线程
        EventLoopGroup group = new NioEventLoopGroup(1);

        Bootstrap bootstrap = new Bootstrap();
        NettyClientHandler nettyClientHandler = new NettyClientHandler();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ByteBuf delimiter = Unpooled.copiedBuffer("!0.0".getBytes());
                        ch.pipeline()
                                //粘包
                                .addLast(new DelimiterBasedFrameDecoder(0xffffff, delimiter))
                                .addLast(new StringDecoder())
                                //10秒没消息时，就发心跳包过去
                                .addLast(new IdleStateHandler(0, 0, 30))
                                .addLast(nettyClientHandler);
                    }
                });
        return bootstrap;
    }
    public synchronized boolean connect(List<String> addresses) {
        boolean allSuccess = true;
        for (String address : addresses) {
            if (channelMap.containsKey(address)) {
                continue;
            }
            String[] ss = address.split(":");
            try {
                ChannelFuture channelFuture = bootstrap.connect(ss[0], Integer.parseInt(ss[1])).sync();
                Channel channel = channelFuture.channel();
                channelMap.put(address, channel);
                channels.add(channel);
            } catch (Exception e) {
                log.error(getClass().toString(), "----该worker连不上----" + address);
                channelMap.put(address, null);
                allSuccess = false;
            }
        }

        return allSuccess;
    }
    
    /**
     * 发送消息
     */
    public boolean sendMessage(byte[] message) {
        if (channels.isEmpty()) {
            log.error("没有可用的连接");
            return false;
        }
        
        try {
            // 选择第一个可用连接发送消息
            Channel channel = channels.get(0);
            if (channel != null && channel.isActive()) {
                ByteBuf byteBuf = Unpooled.copiedBuffer(message);
                channel.writeAndFlush(byteBuf);
                return true;
            } else {
                log.error("连接不可用");
                return false;
            }
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return false;
        }
    }
    
    /**
     * 发送消息到指定地址
     */
    public boolean sendMessage(String address, byte[] message) {
        Channel channel = channelMap.get(address);
        if (channel != null && channel.isActive()) {
            try {
                ByteBuf byteBuf = Unpooled.copiedBuffer(message);
                channel.writeAndFlush(byteBuf);
                return true;
            } catch (Exception e) {
                log.error("发送消息到 {} 失败", address, e);
                return false;
            }
        } else {
            log.error("地址 {} 的连接不可用", address);
            return false;
        }
    }

}
