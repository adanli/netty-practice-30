package org.egg.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class EchoClient {
    private static final int PORT = 8088;

    public static void main(String[] args) throws Exception {
        new EchoClient().execute();
    }

    public void execute() throws InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .remoteAddress(new InetSocketAddress("localhost", PORT))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LineBasedFrameDecoder(1024))
                                .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                .addLast(new StringEncoder(CharsetUtil.UTF_8))
                                .addLast(new SimpleChannelInboundHandler<String>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                        System.out.println(msg);
                                    }

//                                    @Override
//                                    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//                                        System.out.println("注册服务端成功");
//                                        ctx.writeAndFlush("注册服务端成功, hello, i am client");
//                                    }

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        ctx.writeAndFlush("注册服务端成功, hello, i am client" + '\n');
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
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }

    }

}
