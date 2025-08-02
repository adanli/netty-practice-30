package org.egg.netty.codec.demo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientBusinessHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(ClientBusinessHandler.class.getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof HeartbeatResponse) {
                LOGGER.info("接收到服务端心跳响应");
            } else if(msg instanceof BusinessResponse response) {
                LOGGER.info(String.format("接收到服务端业务响应: %s, 状态码: %s", response.getResult(), response.getStatus()));

                // 模拟重试
                if(response.getStatus() != 200) {
                    LOGGER.info("尝试重新发送消息");
                    ctx.writeAndFlush(new BusinessRequest("重试请求"));
                }

            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent event && event.state()== IdleState.WRITER_IDLE) {
            LOGGER.warning("客户端检测到写空闲，发送心跳");
            ctx.writeAndFlush(new HeartbeatRequest());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.log(Level.WARNING, "客户端处理错误", cause);
        ctx.close();
    }
}
