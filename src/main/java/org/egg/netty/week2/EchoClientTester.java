package org.egg.netty.week2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.egg.netty.week2.handler.EnhancedEchoClientHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多客户端压力测试工具
 */
public class EchoClientTester {
    private static final String HOST = "localhost";
    private static final int PORT = 8088;
    private static final int CLIENT_COUNT = 100;
    private static final int MESSAGE_PER_CLIENT = 100;

    private static final AtomicInteger SUCCESS_COUNT = new AtomicInteger(0);

    public static void main(String[] args) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        CountDownLatch latch = new CountDownLatch(CLIENT_COUNT);

        long startTime = System.currentTimeMillis();

        try {
            for (int i = 0; i < CLIENT_COUNT; i++) {
                final int clientId = i;

                new Thread(() -> {
                    try {
                        Bootstrap bootstrap = new Bootstrap()
                                .group(group)
                                .channel(NioSocketChannel.class)
                                .handler(new ChannelInitializer<SocketChannel>() {
                                    @Override
                                    protected void initChannel(SocketChannel ch) throws Exception {
                                        ch.pipeline()
                                                .addLast(new StringDecoder())
                                                .addLast(new StringEncoder())
                                                .addLast(new EnhancedEchoClientHandler(clientId))
                                        ;
                                    }
                                })
                                ;

                        Channel channel = bootstrap.connect(new InetSocketAddress(HOST, PORT)).sync().channel();
                        for (int j = 0; j < MESSAGE_PER_CLIENT; j++) {
                            String msg = "Client-" + clientId + " Message-" + j + "\n";
                            channel.writeAndFlush(msg).sync();
                        }

                        System.out.println(channel.remoteAddress() + "完成，准备断开连接");
                        channel.close().sync();
                        SUCCESS_COUNT.incrementAndGet();

                    } catch (Exception e) {
                        System.err.println("客户端-" + clientId + "异常, " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }


                }).start();

            }

            latch.await();

            long duration = System.currentTimeMillis() - startTime;
            int totalMessages = CLIENT_COUNT * MESSAGE_PER_CLIENT;
            double throughput = totalMessages / (duration/1000.0);

            System.out.println("==========压测结果==========");
            System.out.println("客户端数量: " + CLIENT_COUNT);
            System.out.println("消息总数: " + totalMessages);
            System.out.println("成功客户端: " + SUCCESS_COUNT.get());
            System.out.println("总耗时: " + duration + "ms");
            System.out.println("吞吐量: " + String.format("%.2f", throughput));
            System.out.println("==========压测结果==========");


        } finally {
            group.shutdownGracefully().sync();
        }


    }
}
