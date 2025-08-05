package org.egg.netty.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.egg.netty.rpc.entity.HeartbeatRequest;
import org.egg.netty.rpc.entity.HeartbeatResponse;
import org.egg.netty.rpc.entity.RpcRequest;
import org.egg.netty.rpc.entity.RpcResponse;
import org.egg.netty.rpc.util.RpcCommonUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 协议
 * +----------------+----------------+----------------+----------------+----------------+
 * |  魔数(4字节)    |  版本(1字节)   |  消息类型(1字节) | 保留字段（2字节）| 数据长度(4字节) |  数据内容(N字节) |
 * +----------------+----------------+----------------+----------------+----------------+
 */
public class RpcDecoder extends ReplayingDecoder<Void> {
    private static final Logger LOGGER = Logger.getLogger(RpcDecoder.class.getName());

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNumber = in.readInt();
        if(magicNumber != RpcCommonUtil.MAGIC_NUMBER) {
            throw new RuntimeException("魔数不一致");
        }

        byte version = in.readByte();
        if(version != RpcCommonUtil.VERSION) {
            throw new RuntimeException("版本不一致");
        }

        byte messageType = in.readByte();
        // 仅读取保留字段
        in.readShort();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);

        Object object = this.deserialize(bytes);

        switch (messageType) {
            case RpcCommonUtil.RPC_REQUEST -> {
                if(object instanceof RpcRequest request) {
                    out.add(request);
                } else {
                    throw new RuntimeException("类型不一致: " + messageType);
                }
            }
            case RpcCommonUtil.RPC_RESPONSE -> {
                if(object instanceof RpcResponse response) {
                    out.add(response);
                } else {
                    throw new RuntimeException("类型不一致: " + messageType);
                }
            }
            case RpcCommonUtil.HEARTBEAT_REQUEST -> {
                if(object instanceof HeartbeatRequest request) {
                    out.add(request);
                } else {
                    throw new RuntimeException("类型不一致: " + messageType);
                }
            }
            case RpcCommonUtil.HEARTBEAT_RESPONSE -> {
                if(object instanceof HeartbeatResponse response) {
                    out.add(response);
                } else {
                    throw new RuntimeException("类型不一致: " + messageType);
                }
            }
        }


    }

    public Object deserialize(byte[] bytes) {
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                ) {
            return objectInputStream.readObject();
        } catch (IOException|ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "反序列化失败", e);
            throw new RuntimeException("反序列化失败", e);
        }

    }
}
