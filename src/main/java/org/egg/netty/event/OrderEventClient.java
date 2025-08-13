package org.egg.netty.event;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.Random;

public class OrderEventClient {
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
                                    .addLast(new StringDecoder())
                                    .addLast(new StringEncoder())

                                    .addLast(new SimpleChannelInboundHandler<String>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                            System.out.println("[收到响应]" + msg);
                                        }
                                    })

                            ;
                        }
                    })
                    ;

            Random random = new Random();

            Channel channel = bootstrap.connect(new InetSocketAddress("localhost", 8088)).sync().channel();
            for (int i = 0; i < 10; i++) {
                String reqId = i + "";
                double amount = random.nextDouble(100.0);

                System.out.println("[发送消息] - " + "CREATE_ORDER|"+reqId+"|"+amount+"|thing1、thing2、thing3");
                channel.writeAndFlush("CREATE_ORDER|"+reqId+"|"+amount+"|thing1、thing2、thing3\n");
                Thread.sleep(2000);
            }

            Thread.sleep(5000);
            channel.closeFuture().sync();

        } finally {
            group.shutdownGracefully().sync();
        }

    }
}
