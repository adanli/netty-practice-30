package org.egg.netty.codec.demo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.GZIPOutputStream;

/**
 * 自定义编解码器
 * +----------------+----------------+----------------+----------------+
 * |  魔数 (4字节)   |  版本 (1字节)   |  类型 (1字节)   |  长度 (4字节)   |    是否使用压缩  |
 * +----------------+----------------+----------------+----------------+
 * |                        数据内容 (变长)                         | CRC32
 * +---------------------------------------------------------------+
 */
public class CustomProtocolEncoder extends MessageToByteEncoder<CustomMessage> {
    private final static Logger LOGGER = Logger.getLogger(CustomProtocolEncoder.class.getName());
    private final CRC32 crc32 = new CRC32();

    @Override
    protected void encode(ChannelHandlerContext ctx, CustomMessage msg, ByteBuf out) throws Exception {
        // 1. 序列化消息
        byte[] bytes = null;
        if(msg instanceof BusinessRequest businessRequest) {
            String content = businessRequest.getContent();
            bytes = content.getBytes(Charset.defaultCharset());
        } else if(msg instanceof BusinessResponse businessResponse) {
            String content = String.format("%s: %s", businessResponse.getStatus(), businessResponse.getResult());
            bytes = content.getBytes(Charset.defaultCharset());
        } else { // 心跳消息, 心跳消息没有消息体
            bytes = new byte[0];
        }

        // 2. 构建消息头
        out.writeInt(CustomMessage.MAGIC_NUMBER);
        out.writeByte(CustomMessage.PROTOCOL_VERSION);
        out.writeByte(msg.getMessageType());


        boolean needZip = CustomMessage.needZip(bytes);
        if(needZip) {
            bytes = this.zip(bytes);
        }

        out.writeInt(bytes.length);
        out.writeBoolean(needZip);
        // 3. 写入消息体
        if(bytes.length > 0) {
            out.writeBytes(bytes);
        }
        // 写入CRC32校验和
        crc32.update(bytes, 0, bytes.length);
        long value = crc32.getValue();
        out.writeLong(value);

        System.out.println("编码消息: " + msg);

    }


    /**
     * 使用GZip进行压缩
     */
    private byte[] zip(byte[] bytes) throws Exception{
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gzipOutputStream.write(bytes);
            gzipOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "IO处理异常", e);
            throw new RuntimeException("压缩失败");
        }
    }

}
