package org.egg.netty.codec.demo;

public abstract class CustomMessage {
    protected final static int MAGIC_NUMBER = 0x12345678;
    protected final static byte PROTOCOL_VERSION = 0x01;
    protected final static int zipLength = 5; // 超过该长度，使用压缩算法

    protected abstract byte getMessageType();

    public static boolean needZip(byte[] bytes) {
        if(bytes == null) return false;
        return bytes.length > zipLength;
    }
}
