package org.egg.netty.rpc.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.egg.netty.rpc.RpcClient;
import org.egg.netty.rpc.entity.HeartbeatRequest;
import org.egg.netty.rpc.entity.HeartbeatResponse;
import org.egg.netty.rpc.entity.RpcResponse;

import java.util.concurrent.atomic.AtomicInteger;

public class RpcClientHandler extends ChannelInboundHandlerAdapter {
    private final Bootstrap bootstrap;

    public RpcClientHandler(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof RpcResponse response) {
            System.out.println("客户端接收到rpc响应: " + response.getResult());
        } else if(msg instanceof HeartbeatResponse) {
            System.out.println("客户端接收到心跳响应");
        } else {
            System.err.println("客户端接收到的消息类型异常");
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent event && event.state() == IdleState.WRITER_IDLE) {
            System.err.println("客户端检测到写空闲, 发送心跳");
            ctx.writeAndFlush(new HeartbeatRequest());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.err.println("客户端断开连接, 尝试重连");
        RpcClient.connectWithRetry(bootstrap, new AtomicInteger(0));
    }
}
