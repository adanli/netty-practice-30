package org.egg.netty.async;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.egg.netty.async.handler.ClientBusinessHandler;

import java.net.InetSocketAddress;

public class AsyncClient {
    public static void main(String[] args) throws Exception{
        Bootstrap bootstrap = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch){
                        ch.pipeline()
                                .addLast(new StringDecoder())
                                .addLast(new StringEncoder())
                                .addLast(new ClientBusinessHandler())
                                ;
                    }
                })
                ;

        Channel channel = bootstrap.connect(new InetSocketAddress("localhost", 8088)).sync().channel();
//        channel.writeAndFlush("CALLBACK: 1001");
//        System.out.println("已发送回调查询请求");

        channel.writeAndFlush("BLOCK: 1002");
        System.out.println("已发送阻塞查询请求");

//        channel.close().sync();
    }
}
