package org.egg.netty.rpc.entity;

public class RpcResponse extends RpcMessage{
    private final String result;

    public RpcResponse(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    @Override
    public byte messageType() {
        return 0X02;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "result='" + result + '\'' +
                '}';
    }
}
