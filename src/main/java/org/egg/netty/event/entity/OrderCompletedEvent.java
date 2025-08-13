package org.egg.netty.event.entity;

import io.netty.channel.ChannelHandlerContext;

/**
 * 订单完成事件
 */
public class OrderCompletedEvent extends OrderEvent{
    public OrderCompletedEvent(String orderId, long timestamp, ChannelHandlerContext ctx) {
        super(orderId, timestamp, ctx);
    }
}
