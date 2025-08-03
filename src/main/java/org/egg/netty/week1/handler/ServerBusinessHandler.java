package org.egg.netty.week1.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.egg.netty.week1.entity.BusinessRequest;
import org.egg.netty.week1.entity.BusinessResponse;
import org.egg.netty.week1.entity.HeartRequest;
import org.egg.netty.week1.entity.HeartResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 服务端处理器
 */
public class ServerBusinessHandler extends ChannelInboundHandlerAdapter {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
                if(msg instanceof HeartRequest) {
                    System.out.println("服务端收到心跳请求, 发送心跳回复");
                    ctx.writeAndFlush(new HeartResponse());
                } else if(msg instanceof BusinessRequest businessRequest) {
                    System.out.println("服务端收到业务请求:" + businessRequest);

                    boolean success = new Random().nextInt(100)%2==0;
                    int status = success?200:500;
                    String result = success?"发送成功: " + businessRequest.getContent().toUpperCase():"发送失败: " + businessRequest.getContent();
                    BusinessResponse response = new BusinessResponse(status, result);
                    ctx.writeAndFlush(response);
                }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent event && event.state()== IdleState.READER_IDLE) {
            System.out.println(sdf.format(new Date()) + ": 服务端检测到读空闲, 关闭连接");
            ctx.close();
        }
    }
}
