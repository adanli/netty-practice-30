package org.egg.netty.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.egg.netty.rpc.codec.RpcDecoder;
import org.egg.netty.rpc.codec.RpcEncoder;
import org.egg.netty.rpc.entity.RpcRequest;
import org.egg.netty.rpc.handler.RpcClientHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class RpcClient {
    private static final int MAX_RETRY_TIMES = 3;

    public static void main(String[] args) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(0, 3, 0, TimeUnit.SECONDS))

                                .addLast(new RpcEncoder())
                                .addLast(new RpcDecoder())
                                .addLast(new RpcClientHandler(bootstrap))
                        ;
                    }
                })
        ;

        connectWithRetry(bootstrap, new AtomicInteger(0));
    }

    public static void connectWithRetry(Bootstrap bootstrap, AtomicInteger retryTimes) {
        Timer timer = new HashedWheelTimer();

        ChannelFuture future = bootstrap.connect(new InetSocketAddress("localhost", 8088));
        future.addListener(listener -> {
            if(listener.isSuccess()) {
                System.out.println("客户端连接成功");
                retryTimes.set(0);
                Channel channel = future.channel();
                channel.writeAndFlush(new RpcRequest("org.egg.demo.PrintDemo", "hello", new String[]{}));
                Thread.sleep(1000);
                channel.writeAndFlush(new RpcRequest("org.egg.demo.PrintDemo", "hi", new String[]{}));
            } else {
                int currentRetryTime = retryTimes.incrementAndGet();
                if(currentRetryTime < MAX_RETRY_TIMES) {
                    System.out.printf("尝试第%s次重连%n", currentRetryTime);

                    timer.newTimeout(timeout -> connectWithRetry(bootstrap, retryTimes), 5, TimeUnit.SECONDS);

                } else {
                    System.err.println("达到最大重连次数, 停止重试");
                }
            }
        });
    }

}
