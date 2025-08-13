package org.egg.netty.event.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.egg.netty.event.entity.OrderCompletedEvent;
import org.egg.netty.event.entity.OrderShippedEvent;

/**
 * 通知处理
 */
public class NotificationHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof OrderShippedEvent orderShippedEvent) {

            String orderId = orderShippedEvent.getOrderId();

            try {
                Thread.sleep(1000);

                System.out.printf("[Notification] %s | 已发送发货通知给用户%n", orderId);

                OrderCompletedEvent orderCompletedEvent = new OrderCompletedEvent(
                        orderId, System.currentTimeMillis(), ctx
                );
                ctx.fireUserEventTriggered(orderCompletedEvent);

            } catch (Exception e) {
                Thread.currentThread().interrupt();
                ctx.close();
            }

        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }
}
