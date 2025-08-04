package org.egg.netty.heartbeat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.egg.netty.heartbeat.entity.HeartbeatRequest;
import org.egg.netty.heartbeat.entity.HeartbeatResponse;

import java.util.List;

/**
 * 协议:
 * ｜长度（4字节）｜消息类型(1字节)｜内容（变长）｜
 */
public class HeartbeatDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int length = in.readInt();
        byte messageType = in.readByte();
//        int length = in.readableBytes();
        byte[] bytes = new byte[length];

        in.readBytes(bytes, 0, length);

        switch (messageType) {
            case 0X01 -> out.add(new HeartbeatRequest());
            case 0X02 -> out.add(new HeartbeatResponse());
        }

    }
}
