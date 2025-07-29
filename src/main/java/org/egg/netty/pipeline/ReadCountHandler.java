package org.egg.netty.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

//@ChannelHandler.Sharable
public class ReadCountHandler extends ChannelInboundHandlerAdapter {
    private final AtomicInteger number = new AtomicInteger(0);

    private final Logger logger = Logger.getLogger(ReadCountHandler.class.getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        int count = number.incrementAndGet();
        logger.info("收到消息: " + count + "条");

        ctx.fireChannelRead(msg);
    }
}
