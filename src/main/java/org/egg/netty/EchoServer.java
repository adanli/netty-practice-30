package org.egg.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.egg.netty.pipeline.*;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EchoServer {
    private static final int PORT = 8088;
    private final AtomicLong number = new AtomicLong(0);

    private final Logger logger = Logger.getLogger(EchoServer.class.getName());

    public static void main(String[] args) throws Exception{
        new EchoServer().execute();
    }

    public void execute() throws Exception{
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(boss, worker)
                .localAddress(new InetSocketAddress(PORT))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<>() {

                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline()
                                .addLast("shareCount", new ShareCountHandler())
                                .addLast("decoder", new StringDecoder(CharsetUtil.UTF_8))

                                .addLast("handlerA", new SimpleInboundHandler("A"))
                                .addLast("handlerB", new SimpleInboundHandler("B"))
                                .addLast("handlerC", new SimpleInboundHandler("C"))
                                .addLast("handlerD", new SimpleInboundHandler("D"))

                                .addLast("encoder", new StringEncoder(CharsetUtil.UTF_8))

                                .addLast("outHandlerX", new SimpleOutboundHandler("X"))
                                .addLast("outHandlerY", new SimpleOutboundHandler("Y"))

                        ;
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128) // 连接日志大小
                .childOption(ChannelOption.SO_KEEPALIVE, true)
            ;

        try {
            ChannelFuture cf = bootstrap.bind().sync();
            cf.addListener(listener -> {
               if(listener.isSuccess()) {
                   System.out.println("server start success");
               }
            });
            cf.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            logger.log(Level.INFO, "关闭时异常", e);
        } finally {
            worker.shutdownGracefully().sync();
            boss.shutdownGracefully().sync();
            System.out.println(number.get());
        }


    }

}
