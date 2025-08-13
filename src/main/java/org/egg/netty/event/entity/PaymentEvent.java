package org.egg.netty.event.entity;

import io.netty.channel.ChannelHandlerContext;

/**
 * 支付成功事件
 */
public class PaymentEvent extends OrderEvent{
    private final String paymentId;
    private final double amount;

    public PaymentEvent(String orderId, String paymentId, double amount, long timestamp, ChannelHandlerContext ctx) {
        super(orderId, timestamp, ctx);
        this.paymentId = paymentId;
        this.amount = amount;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public double getAmount() {
        return amount;
    }
}
