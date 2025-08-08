package org.egg.netty.httpfile;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 实现一个高性能的HTTP文件服务器，通过以下优化手段提升性能：
 *  1. 线程池优化：调整EventLoop线程数量
 *  2. TCP参数优化：调整SO_BACKLOG等参数
 *  3. 内存泄漏检测：使用Netty自带工具检测内存泄漏
 *  4. 压力测试：使用JMeter进行性能测试和调优
 */
public class HttpFileServer {
    public static void main(String[] args) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()

                            ;
                        }
                    })
                    ;

            bootstrap.bind(8088).sync();

        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }

    }
}
