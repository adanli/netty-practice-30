package org.egg.netty.pool;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.ThreadLocalRandom;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;


public class EchoServer {
    public static void main(String[] args) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();

                        // 添加心跳检测
                        pipeline.addLast("idleStateHandler", new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));

                        // 添加编解码器
                        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                        pipeline.addLast("stringDecoder", new StringDecoder(StandardCharsets.UTF_8));
                        pipeline.addLast("stringEncoder", new StringEncoder(StandardCharsets.UTF_8));

                        // 添加业务处理器
                        pipeline.addLast("echoHandler", new SimpleChannelInboundHandler<String>(){

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                // 如果是心跳消息，直接忽略
                                if ("HEARTBEAT".equals(msg)) {
                                    return;
                                }

                                // 模拟处理延迟
                                try {
                                    Thread.sleep(ThreadLocalRandom.current().nextInt(100));
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }

                                // 回显消息
                                ctx.writeAndFlush("Echo: " + msg);
                            }
                        });
                    }
                })
                .bind(8080).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
