package org.egg.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.egg.netty.http.handler.HttpRequestHandler;

import javax.net.ssl.KeyManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

public class HttpServer {
    private final static String FILE_PATH = "D:\\code\\java\\practice\\netty-practice-30\\src\\main\\resources\\secret-document.txt";

    public static void main(String[] args) throws Exception{
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
//                                    .addLast(new LoggingHandler(LogLevel.INFO))

//                                    .addLast(sslContext.newHandler(ch.alloc()))

                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(65535))

                                    .addLast(new ChunkedWriteHandler())

                                    .addLast(new HttpRequestHandler(FILE_PATH))
                                    ;
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    ;

            ChannelFuture cf = bootstrap.bind(8088).sync();
            cf.addListener(listener -> {
                if(listener.isSuccess()) {
                    System.out.printf("服务端启动成功%n");
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
        try (InputStream inputStream = HttpServer.class.getResourceAsStream("/server.p12")){
            keyStore.load(inputStream, "serverpass".toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "serverpass".toCharArray());

        return SslContextBuilder
                .forServer(kmf)
                .protocols("TLSv1.3", "TLSv1.2")
                .build();


    }

}
