package org.egg.netty.week1.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.CharsetUtil;
import org.egg.netty.week1.entity.BusinessRequest;
import org.egg.netty.week1.entity.BusinessResponse;
import org.egg.netty.week1.entity.HeartRequest;
import org.egg.netty.week1.entity.HeartResponse;
import org.egg.netty.week1.util.CustomUtil;

import java.util.List;

/**
 *  +----------------+----------------+----------------+
 *  |  长度 (4字节)   |  魔数 (4字节)   |  类型 (1字节)   |  数据内容 (变长)  |
 *  +----------------+----------------+----------------+----------------+
 *  其中长度自动处理
 */
public class CustomMessageDecoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readInt();
        int magicNumber = in.readInt();
        if(magicNumber != CustomUtil.MAGIC_NUMBER) {
            throw new RuntimeException("魔数不一致");
        }

        byte messageType = in.readByte();
//        int length = in.readableBytes();

        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);

        switch (messageType) {
            case CustomUtil.HEART_REQUEST -> out.add(new HeartRequest());
            case CustomUtil.HEART_RESPONSE -> out.add(new HeartResponse());
            case CustomUtil.BUSINESS_REQUEST -> out.add(new BusinessRequest(new String(bytes, CharsetUtil.UTF_8)));
            case CustomUtil.BUSINESS_RESPONSE -> {
                String content = new String(bytes, CharsetUtil.UTF_8);
                int position = content.indexOf(":");
                if(position == -1) throw new RuntimeException("无效的响应数据结构");

                int status = Integer.parseInt(content.substring(0, position));
                String result = content.substring(position+1);
                out.add(new BusinessResponse(status, result));
            }
        }
        System.out.println("解码完成");
    }
}
