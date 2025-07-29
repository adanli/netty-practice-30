package org.egg.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.egg.netty.pipeline.ServerHandlerA;
import org.egg.netty.pipeline.ServerHandlerB;
import org.egg.netty.pipeline.ServerHandlerC;
import org.egg.netty.pipeline.ServerHandlerD;
import org.egg.netty.pipeline.ServerHandlerE;

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
                                .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                .addLast(new StringEncoder(CharsetUtil.UTF_8))
                                .addLast(new ServerHandler(number))
                                .addLast(new ServerHandlerA())
                                .addLast(new ServerHandlerB())
                                .addLast(new ServerHandlerC())
                                .addLast(new ServerHandlerD())
                                .addLast(new ServerHandlerE())

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
