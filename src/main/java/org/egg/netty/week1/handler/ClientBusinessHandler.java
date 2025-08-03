package org.egg.netty.week1.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.egg.netty.week1.entity.BusinessResponse;

public class ClientBusinessHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /*try {
            if(msg instanceof HeartResponse) {
                System.out.println("客户端收到服务端的心跳响应");
            } else if(msg instanceof BusinessResponse businessResponse) {
                System.out.println("客户端收到业务响应: " + businessResponse);
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }*/
        if(msg instanceof BusinessResponse businessResponse) {
            System.out.println("客户端收到业务响应: " + businessResponse);
        }
        ctx.fireChannelRead(msg);
    }


}
