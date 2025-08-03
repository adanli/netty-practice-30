package org.egg.netty.stickyPacket.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final static Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private final String solutionType;

    public ClientHandler(String solutionType) {
        this.solutionType = solutionType;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof String s) {
            LOGGER.info("客户端收到响应: " + s);
        } else if(msg instanceof ByteBuf buf) {
            buf.readInt();
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes, 0, buf.readableBytes());
            System.out.println("客户端收到响应: " + new String(bytes, Charset.defaultCharset()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.log(Level.WARNING, "客户端异常", cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 发送解决方案类型
        ctx.writeAndFlush("SOLUTION: " + solutionType);
        LOGGER.info(ctx.name() + "使用解决方案: " + solutionType);
    }
}
