package org.egg.netty.heartbeat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.egg.netty.heartbeat.codec.HeartbeatDecoder;
import org.egg.netty.heartbeat.codec.HeartbeatEncoder;
import org.egg.netty.heartbeat.entity.HeartbeatRequest;
import org.egg.netty.heartbeat.entity.HeartbeatResponse;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class HeartbeatServer {
    public static void main(String[] args) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS))

                                    .addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4))
                                    .addLast(new LengthFieldPrepender(4))

                                    .addLast(new HeartbeatDecoder())
                                    .addLast(new HeartbeatEncoder())

                                    .addLast(new ChannelInboundHandlerAdapter(){
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                            if(msg instanceof HeartbeatRequest request) {
                                                System.out.println(request.getMessage());
                                                System.out.println("服务端接收到心跳: " + request.getMessage());
                                                ctx.writeAndFlush(new HeartbeatResponse());
                                            }
                                        }

                                        @Override
                                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                            if(evt instanceof IdleStateEvent event && event.state()== IdleState.READER_IDLE) {
                                                System.out.println("服务端检测到读空闲, 关闭连接");
                                                ctx.close();
                                            } else {
                                                super.userEventTriggered(ctx, evt);
                                            }
                                        }
                                    })
                                    ;
                        }
                    })
                ;

            ChannelFuture cf = bootstrap.bind(new InetSocketAddress(8088)).sync();
            cf.addListener(listener -> {
                if(listener.isSuccess()) {
                    System.out.println("服务端启动成功");
                }
            });

            cf.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }

    }
}
