package org.egg.netty.heartbeat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.egg.netty.heartbeat.entity.HeartbeatMessage;

/**
 * 协议:
 * ｜长度（4字节）｜消息类型(1字节)｜内容（变长）｜
 */
public class HeartbeatEncoder extends MessageToByteEncoder<HeartbeatMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, HeartbeatMessage msg, ByteBuf out) {
        byte[] bytes = msg.getContent().getBytes();

        out.writeInt(bytes.length);
        out.writeByte(msg.messageType());
        out.writeBytes(bytes);

    }
}
