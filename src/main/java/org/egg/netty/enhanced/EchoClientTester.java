package org.egg.netty.enhanced;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class EchoClientTester {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final int CLIENT_COUNT = 100;
    private static final int MESSAGES_PER_CLIENT = 100;

    private static final AtomicInteger successCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        CountDownLatch latch = new CountDownLatch(CLIENT_COUNT);

        long startTime = System.currentTimeMillis();

        try {
            for (int i = 0; i < CLIENT_COUNT; i++) {
                final int clientId = i;
                new Thread(() -> {
                    try {
                        Bootstrap b = new Bootstrap();
                        b.group(group)
                         .channel(NioSocketChannel.class)
                         .handler(new ChannelInitializer<SocketChannel>() {
                             @Override
                             protected void initChannel(SocketChannel ch) {
                                 ch.pipeline().addLast(
                                     new StringDecoder(),
                                     new StringEncoder(),
                                     new EchoClientHandler(clientId));
                             }
                         });

                        Channel channel = b.connect(HOST, PORT).sync().channel();

                        // 发送消息
                        for (int j = 0; j < MESSAGES_PER_CLIENT; j++) {
                            String msg = "Client-" + clientId + " Message-" + j;
                            channel.writeAndFlush(msg).sync();
                        }

                        channel.close().sync();
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        System.err.println("客户端 " + clientId + " 异常: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            latch.await();

            long duration = System.currentTimeMillis() - startTime;
            int totalMessages = CLIENT_COUNT * MESSAGES_PER_CLIENT;
            double throughput = totalMessages / (duration / 1000.0);

            System.out.println("\n====== 压测结果 ======");
            System.out.println("客户端数量: " + CLIENT_COUNT);
            System.out.println("消息总数: " + totalMessages);
            System.out.println("成功客户端: " + successCount.get());
            System.out.println("总耗时: " + duration + "ms");
            System.out.println("吞吐量: " + String.format("%.2f", throughput) + " msg/s");
            System.out.println("=====================");
        } finally {
            group.shutdownGracefully();
        }
    }

    static class EchoClientHandler extends SimpleChannelInboundHandler<String> {
        private final int clientId;

        public EchoClientHandler(int clientId) {
            this.clientId = clientId;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            // 验证回显消息
            if (!msg.startsWith("[Echo] Client-" + clientId)) {
                System.err.println("客户端 " + clientId + " 收到错误响应: " + msg);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.err.println("客户端 " + clientId + " 异常: " + cause.getMessage());
            ctx.close();
        }
    }
}
