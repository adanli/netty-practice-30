package org.egg.netty.codec.multiserializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.egg.netty.codec.multiserializer.serializer.JavaSerializer;
import org.egg.netty.codec.multiserializer.serializer.JsonSerializer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * 协议格式：
 * +----------------+----------------+----------------+----------------+
 * |  魔数 (4字节)   |  序列化格式 (1) |  类型 (1字节)   |  长度 (4字节)   |
 * +----------------+----------------+----------------+----------------+
 * |                        数据内容 (变长)                         |
 * +---------------------------------------------------------------+
 *
 * 序列化格式：
 * 0x01: JSON
 * 0x02: Protobuf
 * 0x03: Java原生序列化
 *
 * 消息类型：
 * 0x10: 业务请求
 * 0x20: 业务响应
 */
public class MultiSerializerEncoder extends MessageToByteEncoder<RequestWrapper> {
    private static final Logger LOGGER = Logger.getLogger(MultiSerializerEncoder.class.getName());

    @Override
    protected void encode(ChannelHandlerContext ctx, RequestWrapper msg, ByteBuf out) throws Exception {
        Object payload = msg.payload();
        byte format = msg.serializerFormat();

        // 序列化消息
        try {
            byte[] bodyBytes = switch (format) {
                case MultiSerializerDemo.SERIALIZER_JSON -> JsonSerializer.serialize(payload);
                case MultiSerializerDemo.SERIALIZER_JAVA -> JavaSerializer.serialize(payload);
                default -> throw new RuntimeException("异常的序列化格式");
            };

            out.writeInt(MultiSerializerDemo.MAGIC_NUMBER);
            out.writeByte(format);
            out.writeByte(0X01);
            out.writeInt(bodyBytes.length);
            out.writeBytes(bodyBytes);

            System.out.println("编码消息 [格式=" + MultiSerializerDemo.getFormatName(format) + "]: " + payload);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, ctx.name() + "编码失败", e);
        }

    }
}
