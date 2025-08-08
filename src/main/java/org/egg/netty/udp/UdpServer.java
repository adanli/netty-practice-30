package org.egg.netty.udp;

import io.netty.bootstrap.Bootstrap;
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
import org.egg.netty.udp.handler.UdpServerHandler;
import org.egg.netty.udp.util.UdpCommonUtil;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 实现一个基于UDP协议的简易聊天室系统：
 *  服务端：接收客户端消息并广播给所有在线用户
 *  客户端：发送消息到服务器并接收其他用户的消息
 *  功能特性：
 *      1. 用户加入/离开通知
 *      2. 消息广播
 *      3. 用户列表维护
 *      4. 心跳检测（防止僵尸用户）
 */
public class UdpServer {
    private static final Map<String, Long> clients = new ConcurrentHashMap<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

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
                                    .addLast(new UdpServerHandler(clients))
                            ;
                        }
                    })
                    // 开启广播
                    .option(ChannelOption.SO_BROADCAST, true)
                    ;

            Channel channel = bootstrap.bind(8088).sync().channel();

            checkHeartbeat(channel);

            channel.closeFuture().await();
            System.out.println("服务端启动成功");

        } finally {
            group.shutdownGracefully().sync();
        }

    }

    /**
     * 心跳检测
     */
    private static void checkHeartbeat(Channel channel) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> clients.forEach((k, v) -> {
            if(System.currentTimeMillis() - v > UdpCommonUtil.MAX_TIMEOUT) {
                String message = "["+sdf.format(new Date())+"]"+"系统通知: " + k + " 许久未活跃，自动退出聊天室";
                String[] parts = k.split(":");
                clients.remove(k);
                channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(message.getBytes(CharsetUtil.UTF_8)),
                        new InetSocketAddress(parts[0], Integer.parseInt(parts[1]))
                        ));
            }
        }), 10, 10, TimeUnit.SECONDS);


    }

}
