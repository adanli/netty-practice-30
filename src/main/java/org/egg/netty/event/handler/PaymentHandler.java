package org.egg.netty.event.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.egg.netty.event.entity.OrderCreatedEvent;
import org.egg.netty.event.entity.PaymentEvent;

import java.util.UUID;

/**
 * 支付处理
 */
public class PaymentHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof OrderCreatedEvent event) {
            try {
                Thread.sleep(1000);

                String paymentId = "Pay-" + UUID.randomUUID().toString().substring(0, 8);

                // 构建支付事件
                PaymentEvent paymentEvent = new PaymentEvent(
                        event.getOrderId(),
                        paymentId,
                        event.getAmount(),
                        System.currentTimeMillis(),
                        ctx
                );
                System.out.printf("[PaymentSuccess] %s | 支付ID: %s | 金额: %.2f%n",
                        event.getOrderId(), paymentId, event.getAmount());

                ctx.fireUserEventTriggered(paymentEvent);

            } catch (Exception e) {
                Thread.currentThread().interrupt();
                ctx.close();
            }

        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }
}
