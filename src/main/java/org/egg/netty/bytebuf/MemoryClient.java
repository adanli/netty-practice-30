package org.egg.netty.bytebuf;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public class MemoryClient {
    public static void main(String[] args) throws Exception{
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0 ,4))
                                    .addLast(new LengthFieldPrepender(4))
                                    .addLast(new SimpleClientHandler())
                                ;
                        }
                    })
                ;

            Channel ch = bootstrap.connect(new InetSocketAddress("localhost", 8088))
                    .sync().channel();

            // 发送测试消息
            for (int i=0; i<20; i++) {
                ByteBuf request = Unpooled.buffer()
                        .writeBytes(("请求 " + i).getBytes(Charset.defaultCharset()));
                ch.writeAndFlush(request);
                TimeUnit.MILLISECONDS.sleep(200);
            }

            ch.close().sync();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }

    }
}
