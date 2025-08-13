package org.egg.netty.protobuf.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.egg.netty.protobuf.entity.User;

public class LoginClientHandler extends SimpleChannelInboundHandler<User.LoginResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, User.LoginResponse response) throws Exception {
        System.out.println("客户端接收消息: " + response.getMessage());
    }
}
