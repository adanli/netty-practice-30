package org.egg.netty.codec;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.egg.netty.codec.serializer.MultiSerializerDecoder;
import org.egg.netty.codec.serializer.MultiSerializerEncoder;
import org.egg.netty.codec.serializer.RequestWrapper;
import org.egg.netty.codec.serializer.SerializerCommonUtil;

import java.net.InetSocketAddress;
import java.util.Random;

public class EchoClient {
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
                                    /*.addLast(new CustomStringDecoder())
                                    .addLast(new CustomStringEncoder())
                                    .addLast(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            ctx.writeAndFlush("hello world!");
                                        }
                                    })*/
                                    .addLast(new MultiSerializerDecoder())
                                    .addLast(new MultiSerializerEncoder())
                            ;
                        }
                    })
                    ;

            Channel channel = bootstrap.connect(new InetSocketAddress("localhost", 8088)).channel();
            System.out.println("连接成功");

            Random random = new Random();
            for (int i=0; i<5; i++) {
                channel.writeAndFlush(
                        new RequestWrapper("hello: " + i, random.nextInt(100)%2==0? SerializerCommonUtil.JAVA_FORMAT:SerializerCommonUtil.JSON_FORMAT)
                );
                Thread.sleep(1000);

            }
            Thread.sleep(5000);

            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }

    }
}
