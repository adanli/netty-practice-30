package org.egg.netty.week2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import org.egg.netty.week2.handler.EnhancedEchoServerHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 重构基础EchoServer实现以下增强功能：
 *  1. 多客户端并发支持：同时处理数百个客户端连接
 *  2. 性能监控：实时统计连接数和吞吐量
 *  3. 调试工具集成：内置诊断接口
 *  4. 资源管理：优雅关闭和泄漏检测
 */
public class EnhancedEchoServer {
    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final AtomicInteger CONNECTION_COUNT = new AtomicInteger(0);
    private static final AtomicInteger MESSAGE_COUNT = new AtomicInteger(0);
    private static NioEventLoopGroup BOSS_GROUP;
    private static NioEventLoopGroup WORKER_GROUP;

    public static void main(String[] args) throws Exception{
        BOSS_GROUP = new NioEventLoopGroup(1, new DefaultThreadFactory("boss"));
        WORKER_GROUP = new NioEventLoopGroup(CORES*2, new DefaultThreadFactory("worker"));

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(BOSS_GROUP, WORKER_GROUP)
                    .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    // 诊断连接器
//                                    .addLast(new DiagnosticInterceptor())

                                    .addLast(new LineBasedFrameDecoder(256))

                                    .addLast(new StringDecoder())
                                    .addLast(new StringEncoder())
                                    .addLast(new EnhancedEchoServerHandler(CONNECTION_COUNT, MESSAGE_COUNT))
                            ;
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_RCVBUF, 1024*1024)
                    .childOption(ChannelOption.SO_SNDBUF, 1024*1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    ;

            ChannelFuture cf = bootstrap.bind(8088).sync();
            cf.addListener(listener -> {
                if(listener.isSuccess()) {
                    System.out.println("服务端启动成功");
                }
            });

            monitorServer();

            addShutdownHook();

            cf.channel().closeFuture().sync();

        } finally {
            gratefulShutdown();
        }

    }

    /**
     * 监控HTTP服务
     */
    public static void monitorServer() {
        new Thread(() -> {

            while (true) {
                try {
                    Thread.sleep(5000);

                    int connectionCount = CONNECTION_COUNT.get();
                    // 计算消息速率
                    int messages = MESSAGE_COUNT.get();
                    int messageRate = messages/5;

                    // 待处理任务数
                    int pendingTasks = calculatePendingTasks();

                    // 计算线程池使用情况
                    int activeThreads = calculateActiveThreads();

                    System.out.println("==========监控数据==========");
                    System.out.println("当前连接数: " + connectionCount);
                    System.out.println("消息处理数: " + messages);
                    System.out.println("工作线程数: " + activeThreads);
                    System.out.println("待处理任务数: " + pendingTasks);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

        }, "monitor-thread").start();
    }

    private static int calculatePendingTasks() {
        final AtomicInteger totalPendingTasks = new AtomicInteger(0);
        WORKER_GROUP.forEach(executor -> {
            if(executor instanceof SingleThreadEventExecutor singleThreadEventExecutor) {
                totalPendingTasks.addAndGet(singleThreadEventExecutor.pendingTasks());
            }
        });

        return totalPendingTasks.get();
    }

    private static int calculateActiveThreads() {
        final AtomicInteger totalActiveThreads = new AtomicInteger(0);
        WORKER_GROUP.forEach(executor -> {
            if(executor!=null && executor.inEventLoop()) {
                totalActiveThreads.incrementAndGet();
            }
        });

        return totalActiveThreads.get();
    }

    /**
     * 关闭钩子
     */
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("接收到关闭信号，开始优雅关闭");
            gratefulShutdown();
        }));
    }

    private static void gratefulShutdown() {
        if (BOSS_GROUP != null) {
            BOSS_GROUP.shutdownGracefully().syncUninterruptibly();
        }
        if (WORKER_GROUP != null) {
            WORKER_GROUP.shutdownGracefully().syncUninterruptibly();
        }
        System.out.println("服务器已关闭");
    }

}
