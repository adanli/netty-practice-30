package org.egg.netty.codec.demo;

/**
 * 心跳请求消息
 */
public class BusinessRequest extends CustomMessage {
    private final String content;

    public BusinessRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    protected byte getMessageType() {
        return 0X03;
    }

    @Override
    public String toString() {
        return "BusinessRequest{content='" + content + "'}";
    }
}
