package org.egg.netty.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleInboundHandler extends ChannelInboundHandlerAdapter {
    private final String name;
    private final Logger logger = Logger.getLogger(SimpleInboundHandler.class.getName());

    public SimpleInboundHandler(String name) {
        this.name = name;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger.info(this.name + " Added to pipeline");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        logger.info(this.name + " Remove from pipeline");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info(this.name + " Channel Active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info(this.name + " Channel Inactive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 1. 修改消息内容
        logger.info(this.name + " receive message: " + msg);

        String modifyMsg = String.format("[%s]", msg);

        // 模拟出站操作
        switch (this.name) {
            case "A" -> ctx.pipeline().addAfter(ctx.name(), "handler-Add-After", new SimpleInboundHandler("handler-add-after"));
            case "C" -> {
                logger.info(this.name + " trigger outbound write");
                ctx.channel().writeAndFlush("Response from " + this.name);
            }
            case "D" -> {
                if(msg instanceof String str) {
                    String reverseStr = this.reverse(str);
                    logger.info(this.name + " reverse message: " + reverseStr);
                }
            }
//            case "B" -> throw new RuntimeException("随机抛出异常");
        }
        ctx.fireChannelRead(modifyMsg);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.log(Level.WARNING, this.name + " error", cause);
        ctx.close();
    }

    private String reverse(String s) {
        char[] chars = s.toCharArray();
        for (int i=0; i<chars.length/2; i++) {
            this.swap(chars, i, chars.length-i-1);
        }
        return new String(chars);
    }

    private void swap(char[] s, int i, int j) {
        char c = s[i];
        s[i] = s[j];
        s[j] = c;
    }

}
