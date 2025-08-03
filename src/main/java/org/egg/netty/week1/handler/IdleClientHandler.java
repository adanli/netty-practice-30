package org.egg.netty.week1.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.egg.netty.week1.Practice1Client;
import org.egg.netty.week1.entity.HeartRequest;
import org.egg.netty.week1.entity.HeartResponse;
import org.egg.netty.week1.util.CustomUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class IdleClientHandler extends ChannelInboundHandlerAdapter {
    private final Bootstrap bootstrap;

    public IdleClientHandler(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof HeartResponse) {
                System.out.println("客户端收到服务端的心跳响应");
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent event && event.state() == IdleState.WRITER_IDLE) {
            System.out.println("客户端检测到写空闲，发送心跳请求");
            ctx.writeAndFlush(new HeartRequest());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端连接已激活, 发送心跳请求");
        ctx.writeAndFlush(new HeartRequest());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端连接已断开");

        /*if(!ctx.channel().eventLoop().isShutdown()) {
//            Thread.sleep(5000);
//            ctx.pipeline().remove(this);
//            ctx.channel().close();
            bootstrap.connect(new InetSocketAddress("localhost", CustomUtil.PORT));
        } else {
            System.err.println("eventLoop已关闭，无法重连");
        }*/

        Practice1Client.connectWithRetry(bootstrap, new AtomicInteger(0));


    }
}
