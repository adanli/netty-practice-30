package org.egg.netty.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.egg.netty.http.handler.HttpClientHandler;

import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

public class HttpClient {
    private static final String DIC_PATH = "D:\\code\\java\\practice\\netty-practice-30\\src\\main\\resources";

    public static void main(String[] args) throws Exception{
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

                                    .addLast(new HttpClientCodec())
                                    .addLast(new HttpObjectAggregator(65535))

                                    .addLast(new ChunkedWriteHandler())

                                    .addLast(new HttpClientHandler(DIC_PATH))
                            ;
                        }
                    })
                    ;

            ChannelFuture cf = bootstrap.connect(new InetSocketAddress("localhost", 8088)).sync();
            cf.addListener(listener -> {
                if(listener.isSuccess()) {
                    System.out.println("客户端连接成功");
                }
            });

            Channel channel  = cf.channel();
            channel.writeAndFlush(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"));

            cf.channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully().sync();
        }

    }

    private static SslContext sslContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream inputStream = HttpClient.class.getResourceAsStream("/client-truststore.p12")){
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
