package org.egg.netty.week2.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

public class DiagnosticInterceptor extends ChannelDuplexHandler {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 诊断日志
//        System.out.println("[诊断日志] - 接收消息: " + msg.toString().trim());
        /*try {
            ctx.fireChannelRead(msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }*/
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 诊断日志
//        System.out.println("[诊断日志] - 发送消息: " + msg.toString().trim());
        ctx.write(ctx, promise);
    }
}
