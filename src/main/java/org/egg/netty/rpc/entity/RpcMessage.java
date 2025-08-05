package org.egg.netty.rpc.entity;

import java.io.Serializable;

public abstract class RpcMessage implements Serializable {
    public abstract byte messageType();
}
