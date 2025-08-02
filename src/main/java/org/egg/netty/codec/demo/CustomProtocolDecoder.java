package org.egg.netty.codec.demo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;

/**
 * 自定义编解码器
 * +----------------+----------------+----------------+----------------+
 * |  魔数 (4字节)   |  版本 (1字节)   |  类型 (1字节)   |  长度 (4字节)   |    是否使用压缩  |
 * +----------------+----------------+----------------+----------------+
 * |                        数据内容 (变长)                         | CRC32
 * +---------------------------------------------------------------+
 */
public class CustomProtocolDecoder extends ReplayingDecoder<Void> {
    private final static Logger LOGGER = Logger.getLogger(CustomProtocolDecoder.class.getName());
    private final CRC32 crc32 = new CRC32();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNumber = in.readInt();
        if(magicNumber != CustomMessage.MAGIC_NUMBER) {
            throw new RuntimeException("协议不一致");
        }

        byte version = in.readByte();
        if(version != CustomMessage.PROTOCOL_VERSION) {
            throw new RuntimeException("版本不一致");
        }

        byte messageType = in.readByte();
        int length = in.readInt();
        boolean useZip = in.readBoolean();

        byte[] bs = new byte[length];
        if(length > 0) {
            in.readBytes(bs, 0, length);


        }
        long crc32 = in.readLong();
        this.checkCRC32(bs, crc32);

        if(useZip) {
            bs = this.unzip(bs);
        }

        CustomMessage customMessage = switch (messageType) {
            case 0X01 -> new HeartbeatRequest();
            case 0X02 -> new HeartbeatResponse();
            case 0X03 -> new BusinessRequest(new String(bs, Charset.defaultCharset()));
            case 0X04 -> {

                String content = new String(bs, Charset.defaultCharset());

                int position = content.indexOf(":");
                if(position == -1) {
                    throw new IllegalArgumentException("响应格式异常: " + content);
                }

                int status = Integer.parseInt(content.substring(0, position));
                String result = content.substring(position+1);
                yield new BusinessResponse(status, result);
            }
            default -> {
                throw new RuntimeException("消息类型不符合要求");
            }
        };

        System.out.println("解码消息: " + customMessage);
        out.add(customMessage);

    }

    private void checkCRC32(byte[] bytes, long dest) {
        crc32.update(bytes, 0, bytes.length);
        long src = crc32.getValue();
        if(src != dest) {
            throw new RuntimeException("数据校验和不一致，数据被破坏");
        }
    }

    /**
     * 使用unzip解压缩
     */
    private byte[] unzip(byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] bs = new byte[1024];
            int length;
            while ((length = gzipInputStream.read(bs)) > 0) {
//            while ((length = gzipInputStream.read(bs, 0, 1024)) > 0) {
                outputStream.write(bs, 0, length);
            }

            inputStream.close();
            return outputStream.toByteArray();

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "解压失败", e);
            throw new RuntimeException("解压失败");
        }
    }

}
