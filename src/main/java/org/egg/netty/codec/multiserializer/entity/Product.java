package org.egg.netty.codec.multiserializer.entity;

import java.io.Serializable;

public record Product(String name, int quantity, double price) implements Serializable {
    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
