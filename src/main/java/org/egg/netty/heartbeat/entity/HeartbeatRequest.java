package org.egg.netty.heartbeat.entity;

public class HeartbeatRequest extends HeartbeatMessage{
    public HeartbeatRequest() {
        super("PING");
    }

    public String getMessage() {
        return "PING";
    }

    @Override
    public byte messageType() {
        return 0X01;
    }

    @Override
    public String toString() {
        return String.format("HeartbeatRequest: {content=%s}", getContent());
    }

}
