package org.egg.netty.pipeline;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.logging.Logger;

/**
 * 负责读写的处理器
 */
public class DuplexHandler extends ChannelDuplexHandler {
    private final String name;
    private final Logger logger = Logger.getLogger(DuplexHandler.class.getName());

    public DuplexHandler(String name) {
        this.name = name;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info(this.name + " Channel Active");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger.info(this.name + " Added to pipeline");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.info(this.name + " receive message: " + msg);

        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        logger.info(this.name + " writing message: " + msg);

        try {
            ctx.writeAndFlush(msg);
        } finally {
            promise.setSuccess();
        }

    }
}
