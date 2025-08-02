package org.egg.netty.codec.multiserializer;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.egg.netty.codec.multiserializer.entity.OrderRequest;
import org.egg.netty.codec.multiserializer.entity.Product;
import org.egg.netty.codec.multiserializer.handler.ClientBusinessHandler;
import org.egg.netty.codec.multiserializer.handler.ServerBusinessHandler;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 多序列化格式支持案例
 *
 * 协议格式：
 * +----------------+----------------+----------------+----------------+
 * |  魔数 (4字节)   |  序列化格式 (1) |  类型 (1字节)   |  长度 (4字节)   |
 * +----------------+----------------+----------------+----------------+
 * |                        数据内容 (变长)                         |
 * +---------------------------------------------------------------+
 *
 * 序列化格式：
 * 0x01: JSON
 * 0x02: Protobuf
 * 0x03: Java原生序列化
 *
 * 消息类型：
 * 0x10: 业务请求
 * 0x20: 业务响应
 */
public class MultiSerializerDemo {
    private static final int PORT = 8088;
    public static final int MAGIC_NUMBER = 0X12345678;

    // 序列化格式常量
    public static final byte SERIALIZER_JSON = 0X01;
    public static final byte SERIALIZER_JAVA = 0X03;


    public static void main(String[] args) throws Exception{
        new Thread(() -> {
            try {
                startServer();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

        Thread.sleep(1000);

        testSerializerFormat(SERIALIZER_JSON);
        testSerializerFormat(SERIALIZER_JAVA);

    }

    public static void startServer() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ChannelPipeline pipeline = ch.pipeline();

                     // 解决TCP粘包/拆包问题
                     pipeline.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                     pipeline.addLast(new LengthFieldPrepender(4));

                     // 添加自定义编解码器
                     pipeline.addLast(new MultiSerializerDecoder());
                     pipeline.addLast(new MultiSerializerEncoder());

                     // 添加业务处理器
                     pipeline.addLast(new ServerBusinessHandler());
                 }
             });

            ChannelFuture f = b.bind(PORT).sync();
            System.out.println("服务器已启动，监听端口: " + PORT);

            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void startClient(byte format) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();

                            // 解决TCP粘包/拆包问题
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                            pipeline.addLast(new LengthFieldPrepender(4));

                            // 添加自定义编解码器
                            pipeline.addLast(new MultiSerializerDecoder());
                            pipeline.addLast(new MultiSerializerEncoder());

                            // 添加业务处理器
                            pipeline.addLast(new ClientBusinessHandler());
                        }
                    });

            Channel ch = b.connect("localhost", PORT).sync().channel();
            System.out.println("客户端已连接到服务器，使用序列化格式: " + getFormatName(format));

            for (int i = 0; i < 5; i++) {
                Product product = new Product("商品-" + i, ThreadLocalRandom.current().nextInt(10, 1000), ThreadLocalRandom.current().nextDouble(10, 1000));

                OrderRequest request = new OrderRequest("订单-"+i, System.currentTimeMillis(), new Product[]{product});
                System.out.println("客户端发送请求: " + request);
                ch.writeAndFlush(new RequestWrapper(request, format));

                Thread.sleep(1000);
            }
            Thread.sleep(1000);
            ch.close().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static String getFormatName(byte format) {
        return switch (format) {
            case 0X01 -> "JSON";
            case 0X03 -> "JAVA";
            default -> throw new RuntimeException("不存在该序列化格式");
        };
    }

    private static void testSerializerFormat(byte format) throws Exception {
        System.out.println("\n===== 测试序列化格式: " + getFormatName(format) + " =====");
        startClient(format);
    }

}
