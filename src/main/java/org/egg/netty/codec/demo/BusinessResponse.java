package org.egg.netty.codec.demo;

/**
 * 心跳请求消息
 */
public class BusinessResponse extends CustomMessage {
    private final String result;
    private final int status;

    public BusinessResponse(int status, String result) {
        this.status = status;
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public int getStatus() {
        return status;
    }

    @Override
    protected byte getMessageType() {
        return 0X03;
    }

    @Override
    public String toString() {
        return "BusinessRequest{result='" + result + "', status='"+status+"'}";
    }
}
