package org.egg.netty.zeroCopy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.egg.netty.zeroCopy.handler.FileServerHandler;

/**
 * 实现一个高性能文件服务器，对比传统IO与Netty零拷贝技术的性能差异：
 *  1. 传统IO方式：使用堆内存缓冲区传输
 *  2. 零拷贝方式：使用FileRegion和CompositeByteBuf
 *  3. 性能对比：测试不同文件大小下的传输效率
 *  4. 内存优化：减少内存拷贝和GC压力
 */
public class ZeroCopyFileServer {
    private static final int PORT = 8088;
    private static final String BASE_DIR = "E:\\数据";

    public static void main(String[] args) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new HttpServerCodec())

                                    .addLast(new HttpObjectAggregator(300*1024*1024))

                                    .addLast(new ChunkedWriteHandler())

                                    .addLast(new FileServerHandler(BASE_DIR))
                            ;
                        }
                    })

                    ;

            ChannelFuture cf = bootstrap.bind(PORT).sync();
            cf.addListener(listener -> {
                if(listener.isSuccess()) System.out.println("文件服务器启动: http://localhost:" + PORT);
            });

            cf.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }

    }

}
