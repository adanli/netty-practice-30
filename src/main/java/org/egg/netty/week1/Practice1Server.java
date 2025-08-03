package org.egg.netty.week1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.egg.netty.week1.codec.CustomMessageDecoder;
import org.egg.netty.week1.codec.CustomMessageEncoder;
import org.egg.netty.week1.handler.ServerBusinessHandler;
import org.egg.netty.week1.util.CustomUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 服务端
 */
public class Practice1Server {
    public static void main(String[] args) throws Exception{
        new Thread(() -> {
            try {
                startServer();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
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
                                    .addLast(new IdleStateHandler(5, 3, 0, TimeUnit.SECONDS))

                                    .addLast(new LengthFieldBasedFrameDecoder(CustomUtil.MAX_FRAME_LENGTH, 0, 4, 0, 4))
                                    .addLast(new LengthFieldPrepender(4))
                                    .addLast(new CustomMessageDecoder())
                                    .addLast(new CustomMessageEncoder())
                                    .addLast(new ServerBusinessHandler())
                                    ;
                        }
                    })
                    ;

            ChannelFuture f = bootstrap.bind(new InetSocketAddress(CustomUtil.PORT)).sync();

            f.addListener(listener -> {
                if(listener.isSuccess()) {
                    System.out.println("服务端启动成功");
                }
            });

            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }

    }

}
