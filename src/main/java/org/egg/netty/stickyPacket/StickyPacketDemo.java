package org.egg.netty.stickyPacket;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.egg.netty.stickyPacket.handler.ClientHandler;
import org.egg.netty.stickyPacket.handler.StickPacketServerHandler;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * 粘包拆包解决方案实战案例
 * 演示四种解决方案：
 * 1. 基于换行符: LineBasedFrameDecoder
 * 2. 基于分隔符: DelimiterBasedFrameDecoder
 * 3. 固定长度: FixedLengthFrameDecoder
 * 4. 长度字段: LengthFieldBasedFrameDecoder
 */
public class StickyPacketDemo {
    private static final Logger LOGGER = Logger.getLogger(StickyPacketDemo.class.getName());

    private static final int PORT = 8088;
    private static final String DELIMITER = "|";
    private static final int FIXED_LENGTH = 20;
    private static final int MAX_FRAME_LENGTH = 1024;

    public static void main(String[] args) throws Exception{
        new Thread(() -> {
            try {
                startServer();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

        // 等待服务端完全启动
        Thread.sleep(2000);

//        System.out.println();
//        System.out.println();
//        System.out.println();
//        LOGGER.info("-----------LINE--------------");
//        System.out.println();
//        System.out.println();
//        System.out.println();
//        testLineBasedSolution();
//
//        System.out.println();
//        System.out.println();
//        System.out.println();
//        LOGGER.info("-----------DELIMITER--------------");
//        System.out.println();
//        System.out.println();
//        System.out.println();
//        testDelimiterBasedSolution();
//
//        System.out.println();
//        System.out.println();
//        System.out.println();
//        LOGGER.info("-----------FIXED--------------");
//        System.out.println();
//        System.out.println();
//        System.out.println();
//        testFixedLengthBasedSolution();

        System.out.println();
        System.out.println();
        System.out.println();
        LOGGER.info("-----------LENGTH FIELD--------------");
        System.out.println();
        System.out.println();
        System.out.println();
        testLengthFieldSolution();

    }

    public static void startServer() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    // 通用编码器
                                    .addLast(new StringEncoder(CharsetUtil.UTF_8))
                                    .addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 0, 4, 0, 4))
                                    .addLast(new StringDecoder(CharsetUtil.UTF_8))
//                                    .addLast(new LengthFieldPrepender(4))

//                                    .addLast(new StringDecoder(CharsetUtil.UTF_8))

                                    // 业务处理器
                                    .addLast(new StickPacketServerHandler())
                                    ;
                        }
                    })

                    ;

            ChannelFuture f = bootstrap.bind(new InetSocketAddress(PORT)).sync();
            LOGGER.info("粘包拆包解决方案服务器已启动，监听端口: " + PORT);
            LOGGER.info("支持四种解决方案：");
            LOGGER.info("1. 基于换行符 (LineBasedFrameDecoder)");
            LOGGER.info("2. 基于分隔符 (DelimiterBasedFrameDecoder)");
            LOGGER.info("3. 固定长度 (FixedLengthFrameDecoder)");
            LOGGER.info("4. 基于长度字段 (LengthFieldBasedFrameDecoder)");

            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }

    }


    /**
     * 测试基于换行符的解决方案
     */
    public static void testLineBasedSolution() throws Exception {
        LOGGER.info("=====测试基于换行符的解决方案=====");

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LineBasedFrameDecoder(MAX_FRAME_LENGTH))
                                    .addLast(new StringEncoder(Charset.defaultCharset()))
                                    .addLast(new StringDecoder(Charset.defaultCharset()))

                                    .addLast(new ClientHandler("LINE"))
                                    ;
                        }
                    })
                    ;

            Channel channel = bootstrap.connect(new InetSocketAddress("localhost", PORT)).channel();

            // 发送粘包数据（多个消息连在一起）
            for (int i = 0; i < 5; i++) {
                // 模拟粘包：将多个消息合并发送
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 3; j++) {
                    sb.append("消息-").append(i).append("-").append(j).append("\n");
                }
                channel.writeAndFlush(sb.toString());
                System.out.println("客户端发送粘包数据: " + sb.toString().replace("\n", "\\n"));
                Thread.sleep(500);
            }

            channel.close().sync();

        } finally {
            group.shutdownGracefully().sync();
        }


    }

    /**
     * 测试基于分隔符的解决方案
     */
    public static void testDelimiterBasedSolution() throws Exception {
        LOGGER.info("=====测试基于分隔符的解决方案=====");

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new DelimiterBasedFrameDecoder(MAX_FRAME_LENGTH, Unpooled.copiedBuffer(DELIMITER, Charset.defaultCharset())))
                                    .addLast(new StringEncoder(Charset.defaultCharset()))
                                    .addLast(new StringDecoder(Charset.defaultCharset()))

                                    .addLast(new ClientHandler("DELIMITER"))
                                    ;
                        }
                    })
                    ;

            Channel channel = bootstrap.connect(new InetSocketAddress("localhost", PORT)).channel();

            // 发送粘包数据（多个消息连在一起）
            for (int i = 0; i < 5; i++) {
                // 模拟粘包：将多个消息合并发送
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 3; j++) {
                    sb.append("消息-").append(i).append("-").append(j).append(DELIMITER);
                }
                channel.writeAndFlush(sb.toString());
                System.out.println("客户端发送粘包数据: " + sb.toString());
                Thread.sleep(500);
            }

            channel.close().sync();

        } finally {
            group.shutdownGracefully().sync();
        }


    }

    /**
     * 测试基于固定长度的解决方案
     */
    public static void testFixedLengthBasedSolution() throws Exception {
        LOGGER.info("=====测试基于固定长度的解决方案=====");

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new FixedLengthFrameDecoder(FIXED_LENGTH))
                                    .addLast(new StringEncoder(Charset.defaultCharset()))
                                    .addLast(new StringDecoder(Charset.defaultCharset()))

                                    .addLast(new ClientHandler("FIXED"))
                                    ;
                        }
                    })
                    ;

            Channel channel = bootstrap.connect(new InetSocketAddress("localhost", PORT)).channel();

           // 发送粘包数据（多个消息连在一起）
            for (int i = 0; i < 5; i++) {
                // 模拟粘包：将多个消息合并发送
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 3; j++) {
                    String msg = String.format("消息-%02d-%02d", i, j);
                    // 填充到固定长度
                    msg = String.format("%-" + FIXED_LENGTH + "s", msg);
                    sb.append(msg);
                }
                channel.writeAndFlush(sb.toString());
                System.out.println("客户端发送粘包数据: " + sb.toString());
                Thread.sleep(500);
            }

            channel.close().sync();

        } finally {
            group.shutdownGracefully().sync();
        }


    }

    /**
     * 测试基于长度字段的解决方案
     */
    public static void testLengthFieldSolution() throws Exception {
        LOGGER.info("=====测试基于长度字段的解决方案=====");

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
//                                    .addLast(new StringEncoder(CharsetUtil.UTF_8))
                                    .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                    /*.addLast(new LengthFieldBasedFrameDecoder(
                                            MAX_FRAME_LENGTH, // 最大长度
                                            0, // 长度字段偏移量
                                            4, // 长度字段长度(4字节)
                                            0, // 长度调整值
                                            4  // 剥离长度字段
                                    ))
                                    .addLast(new LengthFieldPrepender(4))*/


                                    .addLast(new ClientHandler("LENGTH_FIELD"))
                                    ;
                        }
                    })
                    ;

            Channel channel = bootstrap.connect(new InetSocketAddress("localhost", PORT)).channel();

            // 发送粘包数据（多个消息连在一起）
            for (int i = 0; i < 5; i++) {
                ByteBuf buf = Unpooled.buffer();
                for (int j = 0; j < 3; j++) {
                    String msg = "消息-" + i + "-" + j;
                    byte[] bytes = msg.getBytes(CharsetUtil.UTF_8);

                    // 写入长度字段(4字节)
                    buf.writeInt(bytes.length);
                    // 写入内容
                    buf.writeBytes(bytes);

                }
                channel.writeAndFlush(buf);
//                buf.retain();
//                System.out.println("客户端发送粘包数据: " + Arrays.toString(buf.toString(CharsetUtil.UTF_8).split("(?<=\\G.{20})")));
//                byte[] bytes = new byte[buf.readableBytes()];
//                buf.readBytes(bytes, 0, buf.readableBytes());
//                System.out.println("客户端发送粘包数据: " + new String(bytes, CharsetUtil.UTF_8));

                Thread.sleep(1000);

            }

            channel.close().sync();

        } finally {
            group.shutdownGracefully().sync();
        }


    }

}
