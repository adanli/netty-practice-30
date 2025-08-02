package org.egg.netty.codec.retry;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.egg.netty.codec.retry.entity.BusinessRequest;
import org.egg.netty.codec.retry.entity.BusinessResponse;
import org.egg.netty.codec.retry.entity.RetryMessage;

import java.nio.charset.Charset;

/**
 *
 * 协议格式：
 * +----------------+----------------+----------------+----------------+
 * |  魔数 (4字节)   |  消息类型 (1)   |  序列号长度 (1) |  序列号 (变长)   |
 * +----------------+----------------+----------------+----------------+
 * |                        数据内容 (变长)                         |
 * +---------------------------------------------------------------+
 *
 * 消息类型：
 * 0x01: 业务请求
 * 0x02: 业务响应
 * 0x03: 心跳请求
 * 0x04: 心跳响应
 */
public class RetryMessageEncoder extends MessageToByteEncoder<RetryMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RetryMessage msg, ByteBuf out) throws Exception {
        // 序列化消息体
        byte[] bytes = null;

        if(msg instanceof BusinessRequest request) {
            bytes = request.getContent().getBytes(Charset.defaultCharset());
        } else if(msg instanceof BusinessResponse response) {
            bytes = String.format("%s:%s", response.getStatus(), response.getResult()).getBytes(Charset.defaultCharset());
        } else {
            bytes = new byte[0]; // 心跳消息
        }

        // 序列号
        byte[] seqIdBytes = msg.getSequenceId().getBytes(Charset.defaultCharset());
        int seqLength = seqIdBytes.length;

        // 构建消息头
        out.writeInt(MessageRetryDemo.MAGIC_NUMBER);
        out.writeByte(msg.getMessageType());
        out.writeByte(seqLength);
        out.writeBytes(seqIdBytes);

        // 写入消息体
        out.writeBytes(bytes);
        System.out.println("编码消息: " + msg);


    }
}
