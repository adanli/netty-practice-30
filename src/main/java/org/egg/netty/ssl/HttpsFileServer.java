package org.egg.netty.ssl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.egg.netty.ssl.handler.FileServerHandler;

import javax.net.ssl.KeyManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyStore;

public class HttpsFileServer {
    private final static File FILE = new File("/Users/adan/code/egg/netty-practice-30/src/main/resources/secret-document.txt");

    static {
        try {
            Files.writeString(FILE.toPath(), "Top Secret: JDK17 Netty SSL Demo", Charset.defaultCharset());
        } catch (IOException ignore) {

        }
    }

    public static void main(String[] args) throws Exception{
        SslContext sslContext = sslContext();

        new ServerBootstrap()
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(sslContext.newHandler(ch.alloc()))
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(65535))
                                .addLast(new FileServerHandler(FILE))
                                ;
                    }
                })
                .bind(new InetSocketAddress(8088))
                .sync()
                .addListener(listener -> {
                    if (listener.isSuccess()) {
                        System.out.println("服务端启动成功: https://localhost:8088");
                    }
                })
        ;

    }

    private static SslContext sslContext() throws Exception{
        // 加载信任库
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream inputStream = HttpsFileServer.class.getResourceAsStream("/server.p12")){
            keyStore.load(inputStream, "serverpass".toCharArray());
        }

        // 初始化KeyManagerFactory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm()
        );
        kmf.init(keyStore, "serverpass".toCharArray());

        return SslContextBuilder.forServer(kmf)
                .protocols("TLSv1.3", "TLSv1.2")
                .build();

    }

}
