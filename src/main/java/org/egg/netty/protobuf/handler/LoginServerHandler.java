package org.egg.netty.protobuf.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.egg.netty.protobuf.entity.User;

public class LoginServerHandler extends SimpleChannelInboundHandler<User.LoginRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, User.LoginRequest msg) throws Exception {
        boolean isValid = "root".equals(msg.getUsername())
                && "123456".equals(msg.getPassword());

        User.LoginResponse response = User.LoginResponse
                .newBuilder()
                .setSuccess(true)
                .setMessage(isValid?"登录成功":"登录失败, 密码错误")
                .build();

        ctx.writeAndFlush(response);
    }
}
