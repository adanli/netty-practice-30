package org.egg.netty.codec.retry;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.egg.netty.codec.retry.entity.BusinessRequest;
import org.egg.netty.codec.retry.entity.BusinessResponse;

import java.nio.charset.Charset;
import java.util.List;

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
public class RetryMessageDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNumber = in.readInt();
        byte messageType = in.readByte();
        byte seqLength = in.readByte();
        byte[] seqId = new byte[seqLength];
        in.readBytes(seqId, 0, seqLength);
        String sequenceId = new String(seqId, Charset.defaultCharset());

        // 消息体
        int remaining = in.readableBytes();
        byte[] bodyBytes = new byte[remaining];
        in.readBytes(bodyBytes, 0, remaining);

        // 创建消息体对象
        switch (messageType) {
            case 0X01 -> {
                String content = new String(bodyBytes, Charset.defaultCharset());
                BusinessRequest request = new BusinessRequest(content);
                request.setSequenceId(sequenceId);
                out.add(request);
            }
            case 0X02 -> {
                String content = new String(bodyBytes, Charset.defaultCharset());
                int position = content.indexOf(":");
                if(position == -1) throw new RuntimeException("无效的响应格式");
                int status = Integer.parseInt(content.substring(0, position));
                String result = content.substring(position+1);

                BusinessResponse response = new BusinessResponse(result, status);
                response.setSequenceId(sequenceId);
                out.add(response);

            }
            default -> {
                throw new IllegalArgumentException("未知的消息类型: " + messageType);
            }
        }
        System.out.println("解码消息: " + out.get(out.size() - 1));

    }
}
