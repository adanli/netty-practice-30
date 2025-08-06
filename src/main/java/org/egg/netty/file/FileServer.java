package org.egg.netty.file;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.egg.netty.file.handler.FileServerHandler;

import javax.net.ssl.KeyManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

public class FileServer {
    private static final String FILE_PATH = "/Users/adan/code/egg/netty-practice-30/src/main/resources/secret-document.txt";

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        SslContext sslContext = sslContext();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(sslContext.newHandler(ch.alloc()))

                                    .addLast(new StringDecoder())
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new FileServerHandler())
                            ;
                        }
                    })
                    ;

            ChannelFuture cf = bootstrap.bind(8088).sync();
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

    private static SslContext sslContext() throws Exception{
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream inputStream = FileServer.class.getResourceAsStream("/server.p12")){
            keyStore.load(inputStream, "serverpass".toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "serverpass".toCharArray());

        return SslContextBuilder.forServer(kmf)
                .protocols("TLSv1.3", "TLSv1.2")
                .build()
                ;
    }

}
