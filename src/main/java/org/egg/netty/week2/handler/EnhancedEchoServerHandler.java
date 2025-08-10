package org.egg.netty.week2.handler;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class EnhancedEchoServerHandler extends SimpleChannelInboundHandler<String> {
    private final AtomicInteger connectionCount;
    private final AtomicInteger messageCount;

    public EnhancedEchoServerHandler(AtomicInteger connectionCount, AtomicInteger messageCount) {
        this.connectionCount = connectionCount;
        this.messageCount = messageCount;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        messageCount.incrementAndGet();

        ctx.writeAndFlush("[Echo]: " + msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        int count = connectionCount.incrementAndGet();
//        System.out.printf("客户端连接: %s | 当前连接数: %s%n", ctx.channel().remoteAddress(), count);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        int count = connectionCount.decrementAndGet();
//        System.out.printf("客户端断开: %s | 当前连接数: %s%n", ctx.channel().remoteAddress(), count);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("服务端处理异常，断开连接: " + ctx.channel().remoteAddress() + " " + cause);
        ctx.close();
    }
}
