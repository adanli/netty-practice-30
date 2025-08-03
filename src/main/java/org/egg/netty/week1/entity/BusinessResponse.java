package org.egg.netty.week1.entity;

public class BusinessResponse extends CustomMessage{
    private final int status;
    private final String result;

    public BusinessResponse(int status, String result) {
        this.status = status;
        this.result = result;
    }

    @Override
    public byte messageType() {
        return 0X20;
    }

    public int getStatus() {
        return status;
    }

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "BusinessResponse{" +
                "status=" + status +
                ", result='" + result + '\'' +
                '}';
    }
}
