package org.egg.netty.codec.demo;

/**
 * 心跳请求消息
 */
public class HeartbeatResponse extends CustomMessage {
    @Override
    protected byte getMessageType() {
        return 0X02;
    }

    @Override
    public String toString() {
        return "HeartbeatResponse";
    }
}
