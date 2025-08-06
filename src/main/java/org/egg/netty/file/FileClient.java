package org.egg.netty.file;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.egg.netty.file.handler.FileClientHandler;

import java.net.InetSocketAddress;

public class FileClient {
    private static final String SAVE_PATH = "/Users/adan/code/egg/netty-practice-30/src/main/resources/";

   private static final String HOST = "localhost";
    private static final int PORT = 8088;

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new StringEncoder())
                                    .addLast(new FileClientHandler(SAVE_PATH))
                                    ;
                        }
                    })
                    ;

            ChannelFuture cf = bootstrap.connect(new InetSocketAddress(HOST, PORT)).sync();

            cf.addListener(listener -> {
                if(listener.isSuccess()) {
                    System.out.println("客户端连接成功");
                }
            });

            Channel channel = cf.channel();
            channel.writeAndFlush("/Users/adan/code/egg/netty-practice-30/src/main/resources/secret-document.txt");

            channel.closeFuture().sync();

        } finally {
            group.shutdownGracefully().sync();
        }


    }

}
