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
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info(this.name + " Added to pipeline");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        logger.info(this.name + " Remove from pipeline");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info(this.name + " Channel Active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info(this.name + " Channel Inactive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 1. 修改消息内容
        logger.info(this.name + " receive message: " + msg);

        String modifyMsg = String.format("[%s]", msg);
        ctx.fireChannelRead(modifyMsg);

        // 模拟出站操作
        if(this.name.equals("C")) {
            logger.info(this.name + " trigger outbound write");
            ctx.channel().writeAndFlush("Response from " + this.name);
        } else if(this.name.equals("D")) {
            if(msg instanceof String str) {
                String reverseStr = this.reverse(str);
                logger.info(this.name + " reverse message: " + reverseStr);
                ctx.fireChannelRead(reverseStr);
            }
        } else if(this.name.equals("B")) {
            throw new RuntimeException("随机抛出异常");
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
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
