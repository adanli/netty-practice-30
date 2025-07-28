package org.egg.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class EchoServer {
    private static final int PORT = 8088;

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
                .childHandler(new ChannelInitializer<Channel>() {

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
                                        ctx.writeAndFlush("from server: " + msg + '\n');
                                    }

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        System.out.println("server active");
                                        ctx.writeAndFlush("hello world!" + '\n');
                                    }
                                })

                        ;
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128) // 连接日志大小
                .option(ChannelOption.SO_KEEPALIVE, true) // 保持长连接
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
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully().sync();
            boss.shutdownGracefully().sync();
        }


    }

}
