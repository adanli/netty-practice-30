package org.egg.netty.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import org.egg.netty.udp.handler.UdpClientHandler;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8088;
    private static volatile AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new UdpClientHandler())
                            ;
                        }
                    })
                    ;

            Channel channel = bootstrap.bind(0).sync().channel();
            // 加入
            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("/join".getBytes(CharsetUtil.UTF_8)), new InetSocketAddress(HOST, PORT)));

            sendHeartbeat(channel);

            scan(channel);

            channel.closeFuture().await();
        } finally {
            group.shutdownGracefully().sync();
        }

    }

    private static void scan(Channel channel) {
        Scanner scanner = new Scanner(System.in);

        while (running.get()) {
            String line = scanner.nextLine();
            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(line.getBytes(CharsetUtil.UTF_8)), new InetSocketAddress(HOST, PORT)));

            if (line.equals("/quit")) {
                running.set(false);
            }

        }

    }

    /**
     * 发送空包，保持心跳
     */
    private static void sendHeartbeat(Channel channel) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("beat".getBytes(CharsetUtil.UTF_8)), new InetSocketAddress(HOST, PORT)));

        }, 10, 10, TimeUnit.SECONDS);
    }

}
