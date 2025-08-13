package org.egg.netty.event.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.egg.netty.event.entity.OrderCompletedEvent;
import org.egg.netty.event.entity.OrderEvent;
import org.egg.netty.event.util.EventStore;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 事件监控
 */
public class EventMonitorHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(EventMonitorHandler.class.getName());

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 启动事件监听
        ctx.executor().scheduleAtFixedRate(() -> {

            LOGGER.info("=====监控事件=====");
            LOGGER.info("活跃订单数: " + countActiveOrders());
            LOGGER.info("总事件数: " + EventStore.getEventCount());
            LOGGER.info("最近事件: " + getLatestEventType());


        }, 10, 5, TimeUnit.SECONDS);
    }

    private String getLatestEventType() {
        if(EventStore.ORDER_EVENTS.isEmpty()) return "无";
        OrderEvent lastOrder = EventStore.ORDER_EVENTS.get(EventStore.ORDER_EVENTS.size()-1);
        return lastOrder.getClass().getSimpleName() + "-" + lastOrder.getOrderId();
    }

    private long countActiveOrders() {
        return EventStore.ORDER_EVENTS.stream().filter(event -> !(event instanceof OrderCompletedEvent) )
                .map(OrderEvent::getOrderId)
                .distinct()
                .count();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof OrderCompletedEvent orderCompletedEvent) {
            System.out.printf("[Monitor] 订单完成: %s%n", orderCompletedEvent.getOrderId());
        }
        ctx.fireUserEventTriggered(evt);
    }
}
