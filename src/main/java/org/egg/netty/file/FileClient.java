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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.egg.netty.file.handler.FileClientHandler;

import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

public class FileClient {
    private static final String SAVE_PATH = "/Users/adan/code/egg/netty-practice-30/src/main/resources/";

   private static final String HOST = "localhost";
    private static final int PORT = 8088;

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        SslContext sslContext = sslContext();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(sslContext.newHandler(ch.alloc()))

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

    private static SslContext sslContext() throws Exception{
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try (InputStream inputStream = FileClient.class.getResourceAsStream("/client-truststore.p12")){
            keyStore.load(inputStream, "clientpass".toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        return SslContextBuilder.forClient()
                .trustManager(tmf)
                .protocols("TLSv1.3", "TLSv1.2")
                .build();

    }

}
