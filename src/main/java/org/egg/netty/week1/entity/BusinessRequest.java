package org.egg.netty.week1.entity;

public class BusinessRequest extends CustomMessage{
    private final String content;

    public BusinessRequest(String content) {
        this.content = content;
    }

    @Override
    public byte messageType() {
        return 0X10;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "BusinessRequest{" +
                "content='" + content + '\'' +
                '}';
    }
}
