package org.egg.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.egg.netty.websocket.handler.StockWebSocketHandler;

public class StockServer {
    private static final String WEBSOCKET_PATH = "/stocks";

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(65535))
                                    .addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH))

                                    .addLast(new StockWebSocketHandler())

                            ;
                        }
                    })
                    .bind(8088).sync().addListener(listener -> {
                        if(listener.isSuccess()) {
                            System.out.println("服务端启动成功");
                        }
                    }).channel().closeFuture().sync();
                    ;


        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }

    }
}
