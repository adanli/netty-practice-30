package org.egg.netty.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import org.egg.netty.chat.handler.UdpServerHandler;
import org.egg.netty.chat.util.ChannelStore;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UdpChatServer {
    // 客户端存储信息, <IP:PORT, 最后活跃时间>
    private static final Map<String, Long> clients = new ConcurrentHashMap<>();
    // 心跳超时时间
    private static final int HEARTBEAT_TIMEOUT = 30;
    private static final int PORT = 8888;

    public static void main(String[] args) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        UdpServerHandler udpServerHandler = new UdpServerHandler(clients);

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(udpServerHandler)
                                    ;
                        }
                    })
                    ;

            Channel channel = bootstrap.bind(PORT).sync().channel();

            // 检查心跳
            startHeartbeatCheck();

            channel.closeFuture().await();

        } finally {
            group.shutdownGracefully().sync();
        }

    }

    private static void startHeartbeatCheck() {
        // 定时调度任务
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();

            Iterator<Map.Entry<String, Long>> iterator = clients.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Long> entry = iterator.next();
                if(currentTime - entry.getValue() > HEARTBEAT_TIMEOUT) {
                    System.out.println("用户超时: " + entry.getKey());
                    broadcastMessage("系统通知: " + entry.getKey() + " 退出了聊天室");
                    iterator.remove();
                }

            }

        }, 10, 10, TimeUnit.SECONDS);


    }

    public static void broadcastMessage(String message) {
        ByteBuf buf = Unpooled.copiedBuffer(message.getBytes(CharsetUtil.UTF_8));

        clients.forEach((k, v) -> {
            String[] parts = k.split(":");
            InetSocketAddress address = new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));

            Channel channel = ChannelStore.getChannel();
            if(channel!=null && channel.isActive()) {
                channel.writeAndFlush(new DatagramPacket(buf.retainedDuplicate(), address));
            }

        });

    }

}
