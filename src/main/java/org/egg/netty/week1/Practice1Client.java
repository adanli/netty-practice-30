package org.egg.netty.week1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.egg.netty.week1.codec.CustomMessageDecoder;
import org.egg.netty.week1.codec.CustomMessageEncoder;
import org.egg.netty.week1.entity.BusinessRequest;
import org.egg.netty.week1.handler.ClientBusinessHandler;
import org.egg.netty.week1.handler.IdleClientHandler;
import org.egg.netty.week1.util.CustomUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Practice1Client {
    public static void main(String[] args) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new IdleStateHandler(0, 3, 0, TimeUnit.SECONDS))

                                    .addLast(new LengthFieldBasedFrameDecoder(CustomUtil.MAX_FRAME_LENGTH, 0, 4, 0, 4))
                                    .addLast(new LengthFieldPrepender(4))
                                    .addLast(new CustomMessageEncoder())
                                    .addLast(new CustomMessageDecoder())
                                    .addLast(new IdleClientHandler(bootstrap))
                                    .addLast(new ClientBusinessHandler())
                            ;
                        }
                    })
                ;

            connectWithRetry(bootstrap, new AtomicInteger(0));

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
//            group.shutdownGracefully().sync();
        }

        // 创建事件循环组
        /*EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(0, 3, 0, TimeUnit.SECONDS))

                                .addLast(new LengthFieldBasedFrameDecoder(CustomUtil.MAX_FRAME_LENGTH, 0, 4, 0, 4))
                                .addLast(new LengthFieldPrepender(4))
                                .addLast(new CustomMessageEncoder())
                                .addLast(new CustomMessageDecoder())
                                .addLast(new IdleClientHandler(bootstrap))
                                .addLast(new ClientBusinessHandler())
                        ;
                    }
                });

        // 启动连接
        connectWithRetry(bootstrap, 0);

        Thread.currentThread().join();*/

    }

    private static void print(Bootstrap bootstrap) throws Exception{
        Channel channel = bootstrap.connect(new InetSocketAddress("localhost", CustomUtil.PORT)).sync().channel();
        System.out.println("客户端连接成功");

        // 发送测试消息
        for (int i=0; i<1; i++) {
            String s = String.format("businessRequest: #" + i);
            channel.writeAndFlush(new BusinessRequest(s)).addListener(l -> {
                if(l.isSuccess()) System.out.println("消息发送成功");
                else System.err.println("消息发送失败");
            });
            Thread.sleep(1000);
        }

        // 等待响应处理完成
        Thread.sleep(5000);
        channel.closeFuture().sync();
    }

    public static void connectWithRetry(Bootstrap bootstrap, AtomicInteger attempt) throws Exception{
        ChannelFuture f = bootstrap.connect(new InetSocketAddress("localhost", CustomUtil.PORT));

        f.addListener(new ChannelFutureListener() {
            private final Timer timer = new HashedWheelTimer();
            private final static int MAX_RETRY_TIMES = 3;

                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if(channelFuture.isSuccess()) {
                            System.out.println("客户端连接成功");
                            attempt.set(0);

                            print(bootstrap);

                        } else {
                            int currentAttempt = attempt.incrementAndGet();
                            if(currentAttempt > MAX_RETRY_TIMES) {
                                System.err.println("达到最大重连次数, 停止重试");
                                channelFuture.channel().close();
                                return;
                            }

                            System.out.printf("第%s次重连%n", currentAttempt);

                            // 每5秒重连一次
                            timer.newTimeout(timeout -> {
                                System.out.println("尝试重连");
                                connectWithRetry(bootstrap, attempt);
                            }, 5, TimeUnit.SECONDS);

                        }

                    }
                });
        f.sync().channel().closeFuture().sync();

    }

}
