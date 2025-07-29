package org.egg.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

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
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        ctx.writeAndFlush("hello, i am client");
                                    }
                                })
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
