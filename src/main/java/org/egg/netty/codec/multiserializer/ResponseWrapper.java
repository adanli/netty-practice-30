package org.egg.netty.codec.multiserializer;

public record ResponseWrapper(Object payload, byte serializerFormat) {
}
