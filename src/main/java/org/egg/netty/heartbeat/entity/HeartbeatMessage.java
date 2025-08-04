package org.egg.netty.heartbeat.entity;

/**
 * 0X01 request
 * 0X02 response
 */
public abstract class HeartbeatMessage {
    private String content;

    public HeartbeatMessage(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public abstract byte messageType();
}
