package org.egg.netty.rpc.entity;

public class RpcResponse extends RpcMessage{
    private final Object result;

    public RpcResponse(Object result) {
        this.result = result;
    }

    public Object getResult() {
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
