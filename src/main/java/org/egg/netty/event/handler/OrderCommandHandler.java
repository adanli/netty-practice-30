package org.egg.netty.event.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.egg.netty.event.entity.OrderCreatedEvent;

import java.util.UUID;

/**
 * 订单命令处理
 */
public class OrderCommandHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("[服务端收到消息] " + msg);
        if(msg.startsWith("CREATE_ORDER")) {
            // 解析命令: CREATE_ORDER|userId|amount|item1,item2
            String[] parts = msg.split("\\|");
            if(parts.length<4) {
                ctx.writeAndFlush("ERROR, 无效的订单命令");
                return;
            }

            String orderId = "Order-" + UUID.randomUUID().toString().substring(0, 8);
            String userId = parts[1];
            double amount = Double.parseDouble(parts[2]);
            String items = parts[3];

            // 构建订单命令事件
            OrderCreatedEvent event = new OrderCreatedEvent(
                    orderId, userId, amount, items, System.currentTimeMillis(), ctx
            );

            System.out.printf("[OrderCreated] %s | 用户: %s | 金额: %.2f | 商品: %s%n",
                orderId, userId, amount, items);

            ctx.fireUserEventTriggered(event);

            // 响应客户端
            ctx.writeAndFlush("order_created: " + orderId + "-" + userId);

        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 传递其他事件
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("服务端处理异常: " + cause);
        ctx.close();
    }
}
