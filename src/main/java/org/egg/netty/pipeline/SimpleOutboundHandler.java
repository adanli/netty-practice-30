package org.egg.netty.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.logging.Logger;

public class SimpleOutboundHandler extends ChannelOutboundHandlerAdapter {
    private final String name;
    private final Logger logger = Logger.getLogger(SimpleOutboundHandler.class.getName());

    public SimpleOutboundHandler(String name) {
        this.name = name;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger.info(this.name + " Added to pipeline");
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        logger.info(this.name + " writing message: " + msg);

        try {
            // 修改消息
            String modifyMessage = String.format("{%s}", msg);
            ctx.writeAndFlush(modifyMessage);

        } finally {
            promise.setSuccess();
        }

    }

}
