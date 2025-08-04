package org.egg.netty.heartbeat.entity;

public class HeartbeatResponse extends HeartbeatMessage{
    public HeartbeatResponse() {
        super("PONG");
    }

    @Override
    public byte messageType() {
        return 0X02;
    }

    public String getMessage() {
        return "PONG";
    }

    @Override
    public String toString() {
        return String.format("HeartbeatResponse: {content=%s}", getContent());
    }
}
