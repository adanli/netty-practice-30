package org.egg.netty.codec.multiserializer.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.egg.netty.codec.multiserializer.MultiSerializerDemo;
import org.egg.netty.codec.multiserializer.ResponseWrapper;
import org.egg.netty.codec.multiserializer.entity.OrderRequest;
import org.egg.netty.codec.multiserializer.entity.OrderResponse;
import org.egg.netty.codec.serializer.RequestWrapper;

import java.util.concurrent.ThreadLocalRandom;

public class ServerBusinessHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof RequestWrapper request) {
                Object payload = request.payload();
                if(payload instanceof OrderRequest orderRequest) {
                    // 处理业务请求
                    System.out.println("服务器收到请求 [格式=" +
                            MultiSerializerDemo.getFormatName(request.format()) + "]: " + request);

                    // 创建响应
                    boolean success = ThreadLocalRandom.current().nextBoolean();
                    String message = success?"订单处理成功": "订单处理失败";
                    OrderResponse response = new OrderResponse(orderRequest.orderId(), success, message);
                    ctx.writeAndFlush(new ResponseWrapper(response, request.format()));
                }
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
