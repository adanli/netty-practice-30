package org.egg.netty.enhanced;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class EnhancedEchoServer {
    private static final int PORT = 8080;
    private static final AtomicInteger connectionCount = new AtomicInteger(0);
    private static final AtomicInteger messageCount = new AtomicInteger(0);
    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;

    public static void main(String[] args) throws Exception {
        // 1. 配置线程池
        int cores = Runtime.getRuntime().availableProcessors();
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("boss"));
        workerGroup = new NioEventLoopGroup(cores * 2, new DefaultThreadFactory("worker"));

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)

             // 2. TCP参数优化
             .option(ChannelOption.SO_BACKLOG, 1024)
             .option(ChannelOption.SO_REUSEADDR, true)
             .childOption(ChannelOption.TCP_NODELAY, true)
             .childOption(ChannelOption.SO_KEEPALIVE, true)
             .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)

             // 3. 添加日志和监控
//             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();

                     // 4. 添加诊断拦截器
                     p.addLast(new DiagnosticInterceptor());

                     p.addLast(new StringDecoder(CharsetUtil.UTF_8));
                     p.addLast(new StringEncoder(CharsetUtil.UTF_8));
                     p.addLast(new EchoServerHandler());
                 }
             });

            // 5. 启动监控服务
            startMonitoringServer();

            ChannelFuture f = b.bind(PORT).sync();
            System.out.println("增强版EchoServer启动，端口: " + PORT);

            // 6. 添加优雅关闭钩子
            addShutdownHook();

            f.channel().closeFuture().sync();
        } finally {
            gracefulShutdown();
        }
    }

    // EchoServer处理器
    static class EchoServerHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            // 7. 消息计数
            messageCount.incrementAndGet();

            // 8. 回显消息
            ctx.writeAndFlush("[Echo] " + msg);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            // 9. 连接计数
            int count = connectionCount.incrementAndGet();
//            System.out.println("客户端连接: " + ctx.channel().remoteAddress() +
//                              " | 当前连接数: " + count);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            int count = connectionCount.decrementAndGet();
//            System.out.println("客户端断开: " + ctx.channel().remoteAddress() +
//                              " | 当前连接数: " + count);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.err.println("处理异常: " + cause.getMessage());
            ctx.close();
        }
    }

    // 诊断拦截器
    static class DiagnosticInterceptor extends ChannelDuplexHandler {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // 10. 诊断日志
//            System.out.println("接收: " + msg.toString().trim());
            ctx.fireChannelRead(msg);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            // 11. 诊断日志
//            System.out.println("发送: " + msg.toString().trim());
            ctx.write(msg, promise);
        }
    }

    // 启动监控HTTP服务
    private static void startMonitoringServer() {
        new Thread(() -> {
            // 简化实现，实际可用Netty HTTP服务
            while (true) {
                try {
                    Thread.sleep(5000);
                    System.out.println("\n====== 监控数据 ======");
                    System.out.println("当前连接数: " + connectionCount.get());
                    System.out.println("消息处理量: " + messageCount.get());
//                    System.out.println("工作线程数: " + workerGroup.executorCount());
//                    System.out.println("待处理任务: " + workerGroup.pendingTasks());
                    System.out.println("====================\n");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "monitor-thread").start();
    }

    // 添加关闭钩子
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("接收到关闭信号，开始优雅关闭...");
            gracefulShutdown();
        }));
    }

    // 优雅关闭
    private static void gracefulShutdown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
        System.out.println("服务器已关闭");
    }
}
