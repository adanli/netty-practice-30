package org.egg.netty.week2.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class EnhancedEchoClientHandler extends SimpleChannelInboundHandler<String> {
    private final int clientId;

    public EnhancedEchoClientHandler(int clientId) {
        this.clientId = clientId;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // 验证回显消息
        if(msg.startsWith("[Echo] Client-" + clientId)) {
            System.err.println("客户端-" + clientId + " 收到错误响应: " + msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("客户端-" + clientId + "异常: " + cause);
        ctx.close();
    }
}
