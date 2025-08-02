package org.egg.netty.codec.retry.entity;

public class BusinessResponse extends RetryMessage{
    private String result;
    private int status;

    public BusinessResponse(String result, int status) {
        this.result = result;
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public byte getMessageType() {
        return 0X02;
    }

    @Override
    public String toString() {
        return String.format("BusinessRes{seqId: %s, status: %s, result: %s}", getSequenceId(), status, result);
    }
}
