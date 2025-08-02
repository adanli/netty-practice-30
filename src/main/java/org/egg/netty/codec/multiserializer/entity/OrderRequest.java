package org.egg.netty.codec.multiserializer.entity;

import java.io.Serializable;
import java.util.Arrays;

public record OrderRequest(String orderId, long timestamp, Product[] products) implements Serializable {
    @Override
    public String toString() {
        return "OrderRequest{" +
                "orderId='" + orderId + '\'' +
                ", timestamp=" + timestamp +
                ", products=" + Arrays.toString(products) +
                '}';
    }
}
