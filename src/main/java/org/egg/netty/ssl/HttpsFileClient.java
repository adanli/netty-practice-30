package org.egg.netty.ssl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.egg.netty.ssl.handler.FileClientHandler;

import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

public class HttpsFileClient {
    public static void main(String[] args) throws Exception{
        SslContext sslContext = sslContext();

        Bootstrap bootstrap = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(sslContext.newHandler(ch.alloc()))

                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(65535))
                                .addLast(new FileClientHandler())

//                                .addLast(new StringDecoder())
//                                .addLast(new StringEncoder())
//                                .addLast(new EchoClientHandler())

                        ;
                    }
                })
                ;
        ChannelFuture cf = bootstrap.connect(new InetSocketAddress("localhost", 8088)).sync();

        Channel channel = cf.channel();
        channel.writeAndFlush(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/")
        );
//        channel.writeAndFlush("hello, i am client");


        cf.addListener(listener -> {
                    if(listener.isSuccess()) {
                        System.out.println("客户端连接成功");
                    }
                });

        channel.closeFuture().sync();
    }

    private static SslContext sslContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream inputStream = HttpsFileClient.class.getResourceAsStream("/client-truststore.p12")){
            keyStore.load(inputStream, "clientpass".toCharArray());
        }

        // TrustManagerFactory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
        );
        tmf.init(keyStore);

        return SslContextBuilder.forClient()
                .trustManager(tmf)
                .protocols("TLSv1.3", "TLSv1.2")
                .build();

    }

}
