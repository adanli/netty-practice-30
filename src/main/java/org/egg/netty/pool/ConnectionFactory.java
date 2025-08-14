package org.egg.netty.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.egg.netty.pool.handler.HeartbeatHandler;
import org.egg.netty.pool.handler.ResponseHandler;

import java.util.concurrent.TimeUnit;

public class ConnectionFactory {
    private final String host;
    private final int port;

    public ConnectionFactory(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private final EventLoopGroup group = new NioEventLoopGroup();

    public ChannelFuture createConnection() {
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("idleStateHandler", new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS))
                                .addLast("heartbeatHandler", new HeartbeatHandler())
                        // 编解码器
                                .addLast(new LengthFieldBasedFrameDecoder(1024*1024, 0, 4, 0, 4))
                                .addLast(new LengthFieldPrepender(4))

                                .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                .addLast(new StringEncoder(CharsetUtil.UTF_8))

                                .addLast(new ResponseHandler())

                        ;
                    }
                })
                ;

        return bootstrap.connect(host, port);

    }

    public void shutdownGratefully() {
        group.shutdownGracefully();
    }

}
