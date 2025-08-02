package org.egg.netty.codec.demo;

/**
 * 心跳请求消息
 */
public class HeartbeatRequest extends CustomMessage {
    @Override
    protected byte getMessageType() {
        return 0X01;
    }

    @Override
    public String toString() {
        return "HeartbeatRequest";
    }
}
