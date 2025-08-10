package org.egg.netty.httpfile;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import org.egg.netty.httpfile.handler.HttpFileServerHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 实现一个高性能的HTTP文件服务器，通过以下优化手段提升性能：
 *  1. 线程池优化：调整EventLoop线程数量
 *  2. TCP参数优化：调整SO_BACKLOG等参数
 *  3. 内存泄漏检测：使用Netty自带工具检测内存泄漏
 *  4. 压力测试：使用JMeter进行性能测试和调优
 */
public class HttpFileServer {
    public static void main(String[] args) throws Exception{
        // 动态线程数
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.printf("核心数量: %d%n", cores);

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(cores*2, new DefaultThreadFactory("worker"));

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(65535))
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new HttpFileServerHandler())
                            ;
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 1024)  // 调整连接队列大小
                    .option(ChannelOption.SO_REUSEADDR, true) // 地址重用
                    .childOption(ChannelOption.TCP_NODELAY, true) // 禁用Nagle算法
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 启用KeepAlive
                    .childOption(ChannelOption.SO_RCVBUF, 1024 * 1024) // 接收缓冲区1MB
                    .childOption(ChannelOption.SO_SNDBUF, 1024 * 1024) // 发送缓冲区1MB
                    ;

            ChannelFuture cf = bootstrap.bind(8088).sync();
            cf.addListener(listener -> {
                if (listener.isSuccess()) {
                    System.out.println("服务端启动成功");
                }
            });

            cf.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }

    }
}
