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
import java.nio.file.Files;
import java.security.KeyStore;

public class HttpsFileServerBak {
    private static final File FILE = new File("/Users/adan/code/egg/netty-practice-30/src/main/resources/secret-document.txt");

    static {
        // 创建文件
        try {
            Files.write(FILE.toPath(),  "Top Secret: Netty SSL Demo".getBytes());
        } catch (IOException ignored) {

        }
    }

    public static void main(String[] args) throws Exception {
        // JDK17 兼容的 SSL 配置
        SslContext sslCtx = createSslContext();

        new ServerBootstrap()
            .group(new NioEventLoopGroup())
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline()
                        .addLast(sslCtx.newHandler(ch.alloc()))
                        .addLast(new HttpServerCodec())
                        .addLast(new HttpObjectAggregator(65536))
                        .addLast(new FileServerHandler(FILE));
                }
            })
            .bind(8088).sync();
        System.out.println("HTTPS服务端启动: https://localhost:8088");
    }

    private static SslContext createSslContext() throws Exception {
        // 加载 PKCS12 密钥库
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = HttpsFileServerBak.class.getResourceAsStream("/server.p12")) {
            keyStore.load(in, "serverpass".toCharArray());
        }

        // 初始化 KeyManagerFactory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "serverpass".toCharArray());

        // 构建 SSL 上下文
        return SslContextBuilder.forServer(kmf)
            .protocols("TLSv1.3", "TLSv1.2")
            .build();
    }

}
