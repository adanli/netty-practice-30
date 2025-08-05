package org.egg.netty.rpc.entity;

public class HeartbeatResponse extends RpcMessage{
    @Override
    public byte messageType() {
        return 0X04;
    }

    @Override
    public String toString() {
        return "HeartbeatResponse{}";
    }
}
