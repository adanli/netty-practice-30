package org.egg.netty.codec.retry.entity;

import java.util.UUID;

public abstract class RetryMessage {
    private String sequenceId;

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }

    private long timestamp;

    public RetryMessage() {
        this.sequenceId = generateSequenceId();
        this.timestamp = System.currentTimeMillis();
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private String generateSequenceId() {
        return UUID.randomUUID().toString();
    }

    public abstract byte getMessageType();

}
