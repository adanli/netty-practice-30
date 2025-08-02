package org.egg.netty.codec.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * 协议格式：
 *  +----------------+----------------+----------------+----------------+
 *  |  魔数 (4字节)   |  序列化格式 (1) |  类型 (1字节)   |  长度 (4字节)   |
 *  +----------------+----------------+----------------+----------------+
 *  |                        数据内容 (变长)                         |
 *  +---------------------------------------------------------------+
 */
public class MultiSerializerDecoder extends ReplayingDecoder<RequestWrapper> {
    private final static Logger LOGGER = Logger.getLogger(MultiSerializerDecoder.class.getName());
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNumber = in.readInt();
        if(magicNumber != SerializerCommonUtil.MAGIC_NUMBER) {
            throw new RuntimeException("魔数不一致");
        }

        byte serializerType = in.readByte();
        byte messageType = in.readByte();
        int length = in.readInt();
        byte[] bytes = new byte[length];

        if(length > 0) {
            in.readBytes(bytes, 0, length);
        }

        RequestWrapper wrapper = switch (serializerType) {
            case SerializerCommonUtil.JSON_FORMAT -> {
                Object object = mapper.readValue(bytes, Object.class);
                RequestWrapper wr =  new RequestWrapper(object, SerializerCommonUtil.JSON_FORMAT);
                LOGGER.info("反序列化: " + wr);
                yield wr;
            }
            case SerializerCommonUtil.JAVA_FORMAT -> {
                try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))){
                    Object object = inputStream.readObject();
                    RequestWrapper wr = new RequestWrapper(object, SerializerCommonUtil.JAVA_FORMAT);
                    LOGGER.info("反序列化: " + wr);
                    yield wr;
                }

            }
            default -> throw new RuntimeException("异常的序列化方式");
        };
        out.add(wrapper);
    }
}
