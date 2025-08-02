package org.egg.netty.codec.multiserializer.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.egg.netty.codec.multiserializer.MultiSerializerDemo;
import org.egg.netty.codec.multiserializer.ResponseWrapper;
import org.egg.netty.codec.multiserializer.entity.OrderResponse;

public class ClientBusinessHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof ResponseWrapper response) {
                Object payload = response.payload();

                if(payload instanceof OrderResponse orderResponse) {
                    System.out.println("客户端收到响应 [格式=" +
                            MultiSerializerDemo.getFormatName(response.serializerFormat()) + "]: " + response);

                }

            }


        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
