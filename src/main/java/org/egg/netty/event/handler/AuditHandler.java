package org.egg.netty.event.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.egg.netty.event.entity.OrderEvent;
import org.egg.netty.event.util.EventStore;

/**
 * 审计处理
 */
@ChannelHandler.Sharable
public class AuditHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof OrderEvent event) {
            // 记录事件到存储（实际应用中写入数据库）
            System.out.printf("[Audit] %s | 事件: %s | 时间: %d%n",
                event.getOrderId(), event.getClass().getSimpleName(), event.getTimestamp());

            EventStore.addEvent(event);

        }
        ctx.fireUserEventTriggered(evt);
    }
}
