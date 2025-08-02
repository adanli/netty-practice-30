package org.egg.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 案例目标：
 *  1. 实现一个自定义的编码器，将字符串消息转换为字节流（格式：4字节长度 + 消息内容）。
 */
public class CustomStringEncoder extends MessageToMessageEncoder<String> {
    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(msg.length());
        buf.writeBytes(msg.getBytes(Charset.defaultCharset()));
        out.add(buf);
    }
}
