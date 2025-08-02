package org.egg.netty.codec.multiserializer;

public record RequestWrapper(Object payload, byte serializerFormat){}
