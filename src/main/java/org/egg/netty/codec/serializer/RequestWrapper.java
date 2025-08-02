package org.egg.netty.codec.serializer;

public record RequestWrapper(Object payload, byte format) {
}
