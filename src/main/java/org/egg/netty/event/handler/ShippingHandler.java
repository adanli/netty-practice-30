package org.egg.netty.event.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.egg.netty.event.entity.OrderShippedEvent;
import org.egg.netty.event.entity.PaymentEvent;

import java.util.UUID;

/**
 * 发货处理
 */
public class ShippingHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof PaymentEvent paymentEvent) {
            Thread.sleep(1000);

            try {
                String trackingNumber = "Track-" + UUID.randomUUID().toString().substring(0, 8);
                String address = "北京市海淀区";

                // 构建发货事件
                OrderShippedEvent orderShippedEvent = new OrderShippedEvent(
                        paymentEvent.getOrderId(),
                        trackingNumber,
                        address,
                        System.currentTimeMillis(),
                        ctx
                );

                System.out.printf("[OrderShipped] %s | 物流号: %s | 地址: %s%n",
                        paymentEvent.getOrderId(), trackingNumber, address);
                ctx.fireUserEventTriggered(orderShippedEvent);

            } catch (Exception e) {
                Thread.currentThread().interrupt();
                ctx.close();
            }


        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }
}
