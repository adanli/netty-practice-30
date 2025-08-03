package org.egg.netty.week1.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;
import org.egg.netty.week1.entity.BusinessRequest;
import org.egg.netty.week1.entity.BusinessResponse;
import org.egg.netty.week1.entity.CustomMessage;
import org.egg.netty.week1.util.CustomUtil;

/**
 *  +----------------+----------------+----------------+
 *  |  长度 (4字节)   |  魔数 (4字节)   |  类型 (1字节)   |  数据内容 (变长)  |
 *  +----------------+----------------+----------------+----------------+
 *  其中长度自动处理
 */
public class CustomMessageEncoder extends MessageToByteEncoder<CustomMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, CustomMessage msg, ByteBuf out) throws Exception {
        byte messageType = msg.messageType();

        // 序列化
        byte[] bytes;
        if(msg instanceof BusinessRequest request) {
            bytes = request.getContent().getBytes(CharsetUtil.UTF_8);
        } else if(msg instanceof BusinessResponse response) {
            bytes = String.format("%s:%s", response.getStatus(), response.getResult()).getBytes(CharsetUtil.UTF_8);
        } else {
            bytes = new byte[0];
        }

        out.writeInt(bytes.length);
        out.writeInt(CustomUtil.MAGIC_NUMBER);
        out.writeByte(messageType);
        out.writeBytes(bytes);
        System.out.println("编码消息: " + msg);
    }
}
