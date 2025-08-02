package org.egg.netty.codec.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 协议格式：
 *  +----------------+----------------+----------------+----------------+
 *  |  魔数 (4字节)   |  序列化格式 (1) |  类型 (1字节)   |  长度 (4字节)   |
 *  +----------------+----------------+----------------+----------------+
 *  |                        数据内容 (变长)                         |
 *  +---------------------------------------------------------------+
 */
public class MultiSerializerEncoder extends MessageToByteEncoder<RequestWrapper> {
    private final ObjectMapper mapper = new ObjectMapper();
    private final static Logger LOGGER = Logger.getLogger(MultiSerializerEncoder.class.getName());

    @Override
    protected void encode(ChannelHandlerContext ctx, RequestWrapper msg, ByteBuf out) throws Exception {
        Object payload = msg.payload();
        byte format = msg.format();

        byte[] bytes = switch (format) {
            case SerializerCommonUtil.JSON_FORMAT -> mapper.writeValueAsBytes(payload);
            case SerializerCommonUtil.JAVA_FORMAT -> {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                try (outputStream) {
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(payload);
                    objectOutputStream.close();
                    yield outputStream.toByteArray();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "序列化失败", e);
                    throw new RuntimeException("序列化失败");
                }


            }
            default -> throw new RuntimeException("异常的序列化类型");
        };

        out.writeInt(SerializerCommonUtil.MAGIC_NUMBER);
        out.writeByte(format);
        out.writeByte(0X01);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);

    }
}
