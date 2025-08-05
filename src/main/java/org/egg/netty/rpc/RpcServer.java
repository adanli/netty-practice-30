package org.egg.netty.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.egg.netty.rpc.codec.RpcDecoder;
import org.egg.netty.rpc.codec.RpcEncoder;
import org.egg.netty.rpc.handler.RpcServerHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class RpcServer {
    public static void main(String[] args) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS))

                                    .addLast(new RpcEncoder())
                                    .addLast(new RpcDecoder())
                                    .addLast(new RpcServerHandler())
                                    ;
                        }
                    })
                    ;


            ChannelFuture cf = bootstrap.bind(new InetSocketAddress(8088)).sync();
            cf.addListener(listener -> {
                if(listener.isSuccess()) {
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
