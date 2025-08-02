package org.egg.netty.codec;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.egg.netty.codec.serializer.MultiSerializerDecoder;
import org.egg.netty.codec.serializer.MultiSerializerEncoder;
import org.egg.netty.codec.serializer.RequestWrapper;

import java.net.InetSocketAddress;

public class EchoServer {
    public static void main(String[] args) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    /*.addLast(new CustomStringDecoder())
                                    .addLast(new CustomStringEncoder())
                                    .addLast(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            System.out.println(msg);
                                        }
                                    })*/
                                    .addLast(new MultiSerializerDecoder())
                                    .addLast(new MultiSerializerEncoder())
                                    .addLast(new ChannelInboundHandlerAdapter(){
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            if(msg instanceof RequestWrapper wrapper) {
                                                System.out.println(wrapper.payload());
                                            }
                                        }
                                    })
                                    ;

                        }
                    })
                ;

            ChannelFuture f = serverBootstrap.bind(new InetSocketAddress(8088))
                    .sync();

            f.channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
        }

    }
}
