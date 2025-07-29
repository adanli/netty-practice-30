package org.egg.netty.pipeline;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@ChannelHandler.Sharable
public class ShareCountHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = Logger.getLogger(ShareCountHandler.class.getName());
    private final AtomicInteger number = new AtomicInteger(0);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        int count = number.incrementAndGet();
        logger.info(ctx.name() + " total messages: " + count);

        ctx.fireChannelRead(msg);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.log(Level.WARNING, "error count", cause);
        ctx.close();
    }
}
