package org.egg.netty.event.entity;

import io.netty.channel.ChannelHandlerContext;

/**
 * 事件基类
 */
public class OrderEvent {
    private final String orderId;
    private final long timestamp;
    private final ChannelHandlerContext ctx;

    public OrderEvent(String orderId, long timestamp, ChannelHandlerContext ctx) {
        this.orderId = orderId;
        this.timestamp = timestamp;
        this.ctx = ctx;
    }

    public String getOrderId() {
        return orderId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
