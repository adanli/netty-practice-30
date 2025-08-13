package org.egg.netty.protobuf;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.egg.netty.protobuf.entity.User;
import org.egg.netty.protobuf.handler.LoginServerHandler;

public class ProtoServer {
    public static void main(String[] args) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()

                                .addLast(new ProtobufVarint32FrameDecoder()) // 解决沾包、断包问题
                                .addLast(new ProtobufDecoder(User.LoginRequest.getDefaultInstance()))
                                .addLast(new ProtobufVarint32LengthFieldPrepender()) // 在消息头添加Protobuf变长长度
                                .addLast(new ProtobufEncoder())

                                .addLast(new LoginServerHandler())

                                ;
                    }
                })
                .bind(8088).sync().addListener(listener -> {
                    if(listener.isSuccess()) {
                        System.out.println("服务端启动成功");
                    }
                }).channel().closeFuture().sync();;

        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }

    }
}
