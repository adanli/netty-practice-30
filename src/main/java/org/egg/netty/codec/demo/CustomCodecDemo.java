package org.egg.netty.codec.demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 自定义编解码器
 * +----------------+----------------+----------------+----------------+
 * |  魔数 (4字节)   |  版本 (1字节)   |  类型 (1字节)   |  长度 (4字节)   |
 * +----------------+----------------+----------------+----------------+
 * |                        数据内容 (变长)                         | CRC32
 * +---------------------------------------------------------------+
 *
 * 消息类型：
 * 0X01：心跳请求
 * 0X02：心跳响应
 * 0X03：业务请求
 * 0X04：业务响应
 *
 */
public class CustomCodecDemo {
    private final static int PORT = 8088;


    private final static Logger LOGGER = Logger.getLogger(CustomCodecDemo.class.getName());

    public static void main(String[] args) throws Exception{
        // 启动服务器
        new Thread(() -> {
            try {
                startServer();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "启动服务器失败", e);
            }
        }).start();

        // 给服务器启动时间
        Thread.sleep(1000);

        // 启动客户端
        startClient();

    }

    public static void startServer() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    // 空闲状态监测，5秒读空闲
//                                    .addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS))

                                    .addLast(new CustomProtocolDecoder())
                                    .addLast(new CustomProtocolEncoder())
                                    .addLast(new ServerBusinessHandler())
                                    ;
                        }
                    })
                    ;

            ChannelFuture f = serverBootstrap.bind(new InetSocketAddress(PORT)).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }

    }

    public static void startClient() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    // 空闲状态监测，3秒写空闲
                                    .addLast(new IdleStateHandler(0, 3, 0, TimeUnit.SECONDS))

                                    .addLast(new CustomProtocolDecoder())
                                    .addLast(new CustomProtocolEncoder())
                                    .addLast(new ClientBusinessHandler())
                                    ;
                        }
                    })
                    ;

            Channel channel = bootstrap.connect(new InetSocketAddress("localhost", PORT)).channel();
            System.out.println("客户端连接成功");

            // 发送测试消息
            for (int i=0; i<5; i++) {
                String s = String.format("businessRequest: #" + i);
                channel.writeAndFlush(new BusinessRequest(s));
                Thread.sleep(1000);
            }

            // 等待响应处理完成
            Thread.sleep(5000);
            channel.close().sync();

        } finally {
            group.shutdownGracefully().sync();
        }

    }


}
