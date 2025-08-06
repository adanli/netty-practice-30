package org.egg.netty.ssl.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof String s) {
            System.out.println("服务端收到消息: " + s);
            ctx.writeAndFlush("hello, i am server");
        }
    }
}
