package org.egg.netty.codec.retry.entity;

public class BusinessRequest extends RetryMessage{
    private final String content;

    @Override
    public String toString() {
        return String.format("BusinessReq{seqId: %s, content: %s}", getSequenceId(), content);
    }

    public BusinessRequest(String content) {
        this.content = content;
    }

    @Override
    public byte getMessageType() {
        return 0X01;
    }

    public String getContent() {
        return content;
    }
}
