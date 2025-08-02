package org.egg.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 案例目标：
 *  1. 实现一个自定义的解码器，将字节流按照上述格式解析为字符串。
 */
public class CustomStringDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int length = msg.readInt();
        byte[] content = new byte[length];
        msg.readBytes(content, 0, length);
        out.add(new String(content, Charset.defaultCharset()));
    }
}
