package org.egg.netty.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.egg.netty.rpc.codec.RpcDecoder;
import org.egg.netty.rpc.codec.RpcEncoder;
import org.egg.netty.rpc.entity.RpcRequest;
import org.egg.netty.rpc.handler.RpcClientHandler;

import java.net.InetSocketAddress;

/**
 *
 */
public class RpcClient {
    public static void main(String[] args) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new RpcEncoder())
                                    .addLast(new RpcDecoder())
                                    .addLast(new RpcClientHandler())
                                    ;
                        }
                    })
                    ;

            Channel channel = bootstrap.connect(new InetSocketAddress("localhost", 8088)).sync().channel();
            channel.writeAndFlush(new RpcRequest("org.egg.demo.PrintDemo", "hello", new String[]{}));

            Thread.sleep(1000);
            channel.writeAndFlush(new RpcRequest("org.egg.demo.PrintDemo", "hi", new String[]{}));

            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
