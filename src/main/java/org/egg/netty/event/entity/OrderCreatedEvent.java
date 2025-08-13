package org.egg.netty.event.entity;

import io.netty.channel.ChannelHandlerContext;

/**
 * 订单创建事件
 */
public class OrderCreatedEvent extends OrderEvent{
    private final String userId;
    private final double amount;
    private final String items;

    public OrderCreatedEvent(String orderId, String userId, double amount, String items, long timestamp, ChannelHandlerContext ctx) {
        super(orderId, timestamp, ctx);
        this.userId = userId;
        this.amount = amount;
        this.items = items;
    }

    public String getUserId() {
        return userId;
    }

    public double getAmount() {
        return amount;
    }

    public String getItems() {
        return items;
    }
}
