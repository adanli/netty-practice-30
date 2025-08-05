package org.egg.netty.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.egg.netty.rpc.entity.RpcMessage;
import org.egg.netty.rpc.util.RpcCommonUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 协议
 * +----------------+----------------+----------------+----------------+----------------+
 * |  魔数(4字节)    |  版本(1字节)   |  消息类型(1字节) | 保留字段（2字节）| 数据长度(4字节) |  数据内容(N字节) |
 * +----------------+----------------+----------------+----------------+----------------+
 */
public class RpcEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final Logger LOGGER = Logger.getLogger(RpcEncoder.class.getName());

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        byte messageType = msg.messageType();
        byte[] bytes = this.serialize(msg);

        out.writeInt(RpcCommonUtil.MAGIC_NUMBER);
        out.writeByte(RpcCommonUtil.VERSION);
        out.writeByte(messageType);
        out.writeShort(RpcCommonUtil.REMAIN_BYTE);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);

    }

    private byte[] serialize(RpcMessage message) {
        try (
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)){
            objectOutputStream.writeObject(message);
            objectOutputStream.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "序列化失败", e);
            throw new RuntimeException("序列化失败", e);
        }

    }

}
