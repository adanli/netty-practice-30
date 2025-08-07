package org.egg.netty.chat;

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

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UdpChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final String CLIENT_NAME = "用户" + (int)(Math.random() * 1000);
    private static volatile boolean running = true;

    public static void main(String[] args) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) throws Exception {

                        }
                    })
                    ;

            Channel channel = bootstrap.bind(0).sync().channel();
            System.out.println("客户端启动，名称: " + CLIENT_NAME);
            System.out.println("输入消息开始聊天，输入/quit退出");

            // 发送加入命令
            sendCommand(channel, "/join");

            // 启动心跳
            startHeartbeat(channel);

            // 启动输入监听
            startInputListener(channel);

            channel.closeFuture().await();
        } finally {
            running = false;
            group.shutdownGracefully().sync();
        }

    }

    /**
     * 发送命令到服务器
     */
    private static void sendCommand(Channel channel, String command) {
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(command.getBytes(CharsetUtil.UTF_8)), new InetSocketAddress(SERVER_HOST, SERVER_PORT)));
    }

    /**
     * 发送消息到服务器
     */
    private static void sendMessage(Channel channel, String message) {
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer((CLIENT_NAME + ": " + message).getBytes(CharsetUtil.UTF_8)), new InetSocketAddress(SERVER_HOST, SERVER_PORT)));
    }

    /**
     * 启动心跳任务
     */
    public static void startHeartbeat(Channel channel) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if(channel.isActive()) {
                // 发送心跳消息（空包）
                channel.writeAndFlush(new DatagramPacket(Unpooled.EMPTY_BUFFER, new InetSocketAddress(SERVER_HOST, SERVER_PORT)));
            }
        }, 10, 10, TimeUnit.SECONDS);

    }

    /**
     * 启动输入监听
     */
    private static void startInputListener(Channel channel) {
        Thread thread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);

            while (running) {
                String input = scanner.nextLine();

                switch (input.toLowerCase()) {
                    case "/quit" -> {
                        sendCommand(channel, input);
                        running = false;
                        channel.close();
                    }
                    case "/list" -> {
                        sendCommand(channel, input);
                    }
                    default -> {
                        sendMessage(channel, input);
                    }
                }
            }

            scanner.close();

        });

        thread.setDaemon(true);
        thread.start();
    }

}
