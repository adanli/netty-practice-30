package org.egg.netty.rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.egg.netty.rpc.entity.HeartbeatRequest;
import org.egg.netty.rpc.entity.HeartbeatResponse;
import org.egg.netty.rpc.entity.RpcRequest;
import org.egg.netty.rpc.entity.RpcResponse;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RpcServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(RpcServerHandler.class.getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof RpcRequest request) {
            System.out.println("服务端接收到RPC消息: " + request);

            // 组装回复
//            RpcResponse response = new RpcResponse(String.format("%s:%s", request.getMethod(), String.join(",", Arrays.toString(request.getArgs()))));
            String className = request.getClassName();
            String method = request.getMethod();
            Object[] args = request.getArgs();
            Object object = this.invoke(className, method, args);

            RpcResponse response = new RpcResponse(object);
            ctx.writeAndFlush(response);
        } else if(msg instanceof HeartbeatRequest request) {
            System.out.println("服务端接收到心跳消息: " + request);

            ctx.writeAndFlush(new HeartbeatResponse());
        } else {
            System.err.println("服务端接收到的消息类型异常");
        }
    }

    public Object invoke(String className, String method, Object[] args) {
        try {
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getConstructor().newInstance();
            Method m = clazz.getMethod(method);
            return m.invoke(instance, args);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "执行方法失败", e);
            throw new RuntimeException("执行方法失败");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent event && event.state() == IdleState.READER_IDLE) {
            System.err.println("服务端检测到读空闲, 断开连接");
            ctx.close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
