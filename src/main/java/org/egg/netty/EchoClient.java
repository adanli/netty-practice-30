package org.egg.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.egg.netty.pipeline.ClientHandlerA;
import org.egg.netty.pipeline.ClientHandlerB;
import org.egg.netty.pipeline.ClientHandlerC;
import org.egg.netty.pipeline.ClientHandlerD;
import org.egg.netty.pipeline.ClientHandlerE;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EchoClient {
    private static final int PORT = 8088;

    private final Logger logger = Logger.getLogger(EchoClient.class.getName());

    public static void main(String[] args) throws Exception {
        new EchoClient().execute();
    }

    public void execute() throws InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .remoteAddress(new InetSocketAddress("localhost", PORT))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch)  {
                        ch.pipeline()
                                .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                .addLast(new StringEncoder(CharsetUtil.UTF_8))
                                .addLast(new ClientHandlerA())
                                .addLast(new ClientHandlerB())
                                .addLast(new ClientHandlerC())
                                .addLast(new ClientHandlerD())
                                .addLast(new ClientHandlerE())
                                .addLast(new ClientHandler())
                        ;
                    }
                })
            ;

        try {
            ChannelFuture cf = bootstrap.connect().sync();
            cf.addListener(listener -> {
                if(listener.isSuccess()) {
                    System.out.println("注册成功");
                }
            });
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.log(Level.INFO, "关闭时异常", e);
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }

    }

}
