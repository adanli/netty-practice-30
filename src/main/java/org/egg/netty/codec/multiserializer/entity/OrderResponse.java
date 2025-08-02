package org.egg.netty.codec.multiserializer.entity;

import java.io.Serializable;

public record OrderResponse(String orderId, boolean success, String message) implements Serializable {
    @Override
    public String toString() {
        return "OrderResponse{" +
                "orderId='" + orderId + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
