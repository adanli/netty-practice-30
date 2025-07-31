package org.egg.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleClientHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = Logger.getLogger(SimpleClientHandler.class.getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(msg instanceof ByteBuf buf) {
            try {
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                System.out.println("服务器响应: " + new String(bytes, Charset.defaultCharset()));
            } finally {
                ReferenceCountUtil.release(buf);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.log(Level.WARNING, SimpleClientHandler.class.getName() + "发生错误", cause);
        ctx.close();
    }

}
