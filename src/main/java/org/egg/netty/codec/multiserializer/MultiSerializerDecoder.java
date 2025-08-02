package org.egg.netty.codec.multiserializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.egg.netty.codec.multiserializer.serializer.JavaSerializer;
import org.egg.netty.codec.multiserializer.serializer.JsonSerializer;
import org.egg.netty.codec.serializer.RequestWrapper;

import java.util.List;
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
public class MultiSerializerDecoder extends ReplayingDecoder<Void> {
    private static final Logger LOGGER = Logger.getLogger(MultiSerializerDecoder.class.getName());

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNumber = in.readInt();
        if (magicNumber != MultiSerializerDemo.MAGIC_NUMBER) {
            throw new RuntimeException("无效的魔数: " + magicNumber);
        }

        byte format = in.readByte();
        byte messageType = in.readByte();
        int length = in.readInt();

        // 消息体
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);

        try {
            // 反序列化
            Object payload = null;
            switch (messageType) {
                case 0X01 -> {
                    switch (format) {
                        case MultiSerializerDemo.SERIALIZER_JSON -> {
                            payload = JsonSerializer.deserialize(bytes);
                            out.add(new RequestWrapper(payload, format));
                        }
                        case MultiSerializerDemo.SERIALIZER_JAVA -> {
                            payload = JavaSerializer.deserialize(bytes);
                            out.add(new RequestWrapper(payload, format));
                        }
                    }
                }
                case 0X02 -> {
                    switch (format) {
                        case MultiSerializerDemo.SERIALIZER_JSON -> {
                            payload = JsonSerializer.deserialize(bytes);
                            out.add(new ResponseWrapper(payload, format));
                        }
                        case MultiSerializerDemo.SERIALIZER_JAVA -> {
                            payload = JavaSerializer.deserialize(bytes);
                            out.add(new ResponseWrapper(payload, format));
                        }
                    }
                }
            }
            System.out.println("解码消息 [格式=" + MultiSerializerDemo.getFormatName(format) + "]: " + payload);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, ctx.name() + "编码失败", e);
        }

    }
}
