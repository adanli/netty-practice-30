package org.egg.netty.stickyPacket.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.egg.netty.stickyPacket.StickyPacketDemo;

import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 服务端业务处理器
 */
public class StickPacketServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(StickyPacketDemo.class.getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof String s) {
            LOGGER.info("服务端收到消息-String: " + s);

            ctx.writeAndFlush("服务端回复-String: " + s);

        } else if(msg instanceof ByteBuf buf) {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes, 0, buf.readableBytes());

            /*ByteBuf byteBuf = Unpooled.buffer();
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);
            ctx.writeAndFlush(byteBuf);*/
            String content = new String(bytes, Charset.defaultCharset());
            LOGGER.info("服务端收到消息-ByteBuf: " + content);
            ctx.writeAndFlush("服务端回复-ByteBuf: " + content);

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.log(Level.WARNING, "服务端异常", cause);
        ctx.close();
    }
}
