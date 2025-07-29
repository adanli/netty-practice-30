package org.egg.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServerHandler extends ChannelInboundHandlerAdapter {
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
    private final AtomicLong number;

    private final Logger logger = Logger.getLogger(ServerHandler.class.getName());

    public ServerHandler(AtomicLong number) {
        this.number = number;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush("hello, i am server");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.log(Level.INFO, "关闭时异常", cause);
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println(msg);
        ctx.writeAndFlush(String.format("[%s] %s", simpleDateFormat.format(new Date()), msg));
        number.addAndGet(1);
        ctx.fireChannelRead(msg);
    }
}
