package org.egg.netty.reconnect;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AutoReconnectClient {
     private static final String HOST = "localhost";
    private static final int PORT = 8088;
    private static final int MAX_RETRIES = 5;
    private static final int BASE_DELAY = 2; // 基础延迟时间(秒)

    public static void main(String[] args) {
        // 创建事件循环组
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            // 创建客户端引导
            Bootstrap bootstrap = createBootstrap(group);

            // 启动连接
            connect(bootstrap, 0);

            // 保持主线程运行
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * 创建客户端引导配置
     */
    private static Bootstrap createBootstrap(EventLoopGroup group) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                 .channel(NioSocketChannel.class)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) {
                         ChannelPipeline pipeline = ch.pipeline();
                         pipeline.addLast(new StringDecoder());
                         pipeline.addLast(new StringEncoder());
                         pipeline.addLast(new SimpleClientHandler());
                     }
                 });
        return bootstrap;
    }

    /**
     * 执行连接操作，支持自动重连
     * @param retryCount 当前重试次数
     */
    private static void connect(Bootstrap bootstrap, int retryCount) {
        bootstrap.connect(HOST, PORT).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) {
                if (future.isSuccess()) {
                    System.out.println("连接服务器成功!");
                } else {
                    System.err.println("连接失败: " + future.cause().getMessage());

                    if (retryCount < MAX_RETRIES) {
                        // 计算重连延迟时间 (指数退避)
                        int delay = BASE_DELAY * (1 << retryCount); // 2, 4, 8, 16, 32秒
                        System.out.println("将在 " + delay + " 秒后尝试重连 (" + (retryCount + 1) + "/" + MAX_RETRIES + ")");

                        // 安排重连任务
                        bootstrap.config().group().schedule(() ->
                            connect(bootstrap, retryCount + 1), delay, TimeUnit.SECONDS);
                    } else {
                        System.err.println("达到最大重连次数，停止尝试");
                        bootstrap.config().group().shutdownGracefully();
                    }
                }
            }
        });
    }

    /**
     * 简单的客户端处理器
     */
    private static class SimpleClientHandler extends ChannelInboundHandlerAdapter {
        private final Random random = new Random();

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            System.out.println("连接已激活");
            // 定时发送消息
            scheduleMessage(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            System.out.println("连接已断开");
            // 注意: 重连逻辑在连接层处理，这里不需要额外操作
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            System.out.println("收到服务器响应: " + msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.err.println("发生异常: " + cause.getMessage());
            ctx.close();
        }

        /**
         * 定时发送消息
         */
        private void scheduleMessage(ChannelHandlerContext ctx) {
            ctx.channel().eventLoop().schedule(() -> {
                if (ctx.channel().isActive()) {
                    String message = "客户端消息-" + System.currentTimeMillis();
                    System.out.println("发送消息: " + message);
                    ctx.writeAndFlush(message);

                    // 随机间隔(1-5秒)发送下一条消息
                    scheduleMessage(ctx);
                }
            }, 1 + random.nextInt(4), TimeUnit.SECONDS);
        }
    }
}
