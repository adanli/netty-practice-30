package org.egg.netty.event;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.egg.netty.event.handler.*;

/**
 * 实现一个基于事件驱动的订单处理系统：
 *  1. 自定义事件：订单创建、支付成功、发货、完成
 *  2. 事件传播：跨Handler传递业务事件
 *  3. 事件监听：实时监控订单状态变化
 *  4. 事件溯源：完整记录订单生命周期
 *
 */
public class OrderEventServer {
    public static void main(String[] args) throws Exception{
        int cores = Runtime.getRuntime().availableProcessors();

        EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("boss"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(cores*2, new DefaultThreadFactory("worker"));

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LineBasedFrameDecoder(1024))

                                    .addLast(new StringDecoder())
                                    .addLast(new StringEncoder())

                                    .addLast(new OrderCommandHandler())
                                    .addLast(new AuditHandler())
                                    .addLast(new PaymentHandler())
                                    .addLast(new AuditHandler())
                                    .addLast(new ShippingHandler())
                                    .addLast(new AuditHandler())
                                    .addLast(new NotificationHandler())
                                    .addLast(new AuditHandler())
                                    .addLast(new EventMonitorHandler())

                            ;
                        }
                    })
                    ;
            bootstrap.bind(8088).sync().addListener(listener -> {
                if(listener.isSuccess()) {
                    System.out.println("服务器启动成功");
                }
            }).channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }

    }
}
