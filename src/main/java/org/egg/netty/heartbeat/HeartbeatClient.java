package org.egg.netty.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.egg.netty.heartbeat.codec.HeartbeatDecoder;
import org.egg.netty.heartbeat.codec.HeartbeatEncoder;
import org.egg.netty.heartbeat.entity.HeartbeatRequest;
import org.egg.netty.heartbeat.entity.HeartbeatResponse;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 支持重连
 */
public class HeartbeatClient {
    private static final int MAX_RETRY_TIMES = 3;
    private static final Logger LOGGER = Logger.getLogger(HeartbeatClient.class.getName());

    public static void main(String[] args) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new IdleStateHandler(0, 3, 0, TimeUnit.SECONDS))

                                    .addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4))
                                    .addLast(new LengthFieldPrepender(4))

                                    .addLast(new HeartbeatDecoder())
                                    .addLast(new HeartbeatEncoder())

                                    .addLast(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                            if(msg instanceof HeartbeatResponse response) {
                                                System.out.println("客户端收到响应: " + response.getMessage());
                                            }
                                        }

                                        @Override
                                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                            if(evt instanceof IdleStateEvent event && event.state()== IdleState.WRITER_IDLE) {
                                                System.out.println("客户端检测到写空闲, 发送心跳请求");
                                                ctx.writeAndFlush(new HeartbeatRequest());
                                            } else {
                                                super.userEventTriggered(ctx, evt);
                                            }
                                        }

                                        @Override
                                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                            System.out.println("客户端断开连接, 尝试重连");
                                            connect(bootstrap, new AtomicInteger(0));
                                        }
                                    })
                                    ;
                        }
                    })
                    ;

            connect(bootstrap, new AtomicInteger(0));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "心跳应用客户端处理失败", e);
        }

    }

    public static void connect(Bootstrap bootstrap, AtomicInteger retryTimes) throws Exception{
        Timer timer = new HashedWheelTimer();
        bootstrap.connect(new InetSocketAddress("localhost", 8088))
                .addListener((ChannelFutureListener) future -> {

                if (future.isSuccess()) {
                    System.out.println("客户端连接成功, 发送心跳");
                    retryTimes.set(0);

                } else {
                    int currentRetryTimes = retryTimes.incrementAndGet();
                    if(currentRetryTimes < MAX_RETRY_TIMES) {
                        System.out.printf("连接失败，尝试重连, 第%s次%n", currentRetryTimes);

                        timer.newTimeout(timeout -> connect(bootstrap, retryTimes), 5, TimeUnit.SECONDS);

                    } else {
                        System.err.println("达到最大重连次数, 停止尝试");

                        future.channel().close();
                        bootstrap.config().group().shutdownGracefully();
                    }


                }

            })
        ;

    }

}
