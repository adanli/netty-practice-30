package org.egg.netty.rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.egg.netty.rpc.entity.HeartbeatResponse;
import org.egg.netty.rpc.entity.RpcResponse;

public class RpcClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof RpcResponse response) {
            System.out.println("客户端接收到rpc响应: " + response.getResult());
        } else if(msg instanceof HeartbeatResponse) {
            System.out.println("客户端接收到心跳响应");
        } else {
            System.err.println("客户端接收到的消息类型异常");
        }
    }
}
