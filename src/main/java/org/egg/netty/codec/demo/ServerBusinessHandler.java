package org.egg.netty.codec.demo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerBusinessHandler extends ChannelInboundHandlerAdapter {
    private final static Logger LOGGER = Logger.getLogger(ServerBusinessHandler.class.getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof HeartbeatRequest) {
                // 处理心跳请求
                LOGGER.info("服务端接收到心跳请求，发送响应");
                ctx.writeAndFlush(new HeartbeatResponse());
            } else if(msg instanceof BusinessRequest request) {
                LOGGER.info("服务端接收到业务请求: " + request.getContent());

                // 模拟业务处理
                int status = new Random().nextInt(100) < 80 ? 200 : 500; // 80%成功率
                String result = status==200?"处理成功: " + request.getContent().toUpperCase():"处理失败: " + request.getContent();
                ctx.writeAndFlush(new BusinessResponse(status, result));
            } else {
                LOGGER.log(Level.WARNING, "收到未知类型的消息");
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent event) {
            if(event.state() == IdleState.READER_IDLE) {
                LOGGER.info("服务器检测到读空闲, 断开连接");
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.log(Level.WARNING, "服务器处理错误", cause);
        ctx.close();
    }
}
