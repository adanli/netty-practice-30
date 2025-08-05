package org.egg.netty.rpc.entity;

import java.util.Arrays;

public class RpcRequest extends RpcMessage{
    private String className;
    private String method;
    private Object[] args;

    public RpcRequest() {
    }

    public String getClassName() {
        return className;
    }

    public RpcRequest(String className, String method, Object[] args) {
        this.className = className;
        this.method = method;
        this.args = args;
    }

    public String getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public byte messageType() {
        return 0X01;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "method='" + method + '\'' +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
