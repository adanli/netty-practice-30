package org.egg.netty.udp.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 接收服务端的响应
 */
public class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buf = msg.content();
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes, 0, buf.readableBytes());
        System.out.println("["+sdf.format(new Date())+"]"+"客户端接收到消息: " + new String(bytes, CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("客户端异常");
        ctx.close();
    }
}
