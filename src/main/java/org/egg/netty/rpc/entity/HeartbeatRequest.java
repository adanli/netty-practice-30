package org.egg.netty.rpc.entity;

public class HeartbeatRequest extends RpcMessage{
    @Override
    public byte messageType() {
        return 0X03;
    }

    @Override
    public String toString() {
        return "HeartbeatRequest{}";
    }
}
