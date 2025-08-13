package org.egg.netty.event.util;

import org.egg.netty.event.entity.OrderEvent;

import java.util.ArrayList;
import java.util.List;

public class EventStore {
    public static final List<OrderEvent> ORDER_EVENTS = new ArrayList<>();

    public static synchronized void addEvent(OrderEvent event) {
        ORDER_EVENTS.add(event);
    }

    public static List<OrderEvent> getEventsById(String orderId) {
        return ORDER_EVENTS.stream().filter(event -> event.getOrderId().equals(orderId))
                .toList();
    }

    public static int getEventCount() {
        return ORDER_EVENTS.size();
    }

}
