package org.egg.netty.event.entity;

import io.netty.channel.ChannelHandlerContext;

/**
 * 发货事件
 */
public class OrderShippedEvent extends OrderEvent{
    private final String trackingNumber;
    private final String address;

    public OrderShippedEvent(String orderId, String trackingNumber, String address, long timestamp, ChannelHandlerContext ctx) {
        super(orderId, timestamp, ctx);
        this.trackingNumber = trackingNumber;
        this.address = address;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getAddress() {
        return address;
    }
}
