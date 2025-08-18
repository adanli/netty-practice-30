package org.egg.netty.pooled;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.egg.netty.pooled.handler.PoolResponseHandler;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 负责创建连接
 */
public class PooledConnectionFactory {
    private static final Logger LOGGER = Logger.getLogger(PooledConnectionFactory.class.getName());

    private final EventLoopGroup group = new NioEventLoopGroup();
    private static final int MAX_FRAME_LENGTH = 1024*1024;
    private final String host;
    private final int port;

    public PooledConnectionFactory(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ChannelFuture createChannel() {
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 0, 4, 0, 4))
                                    .addLast(new LengthFieldPrepender(4))
                                    .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                    .addLast(new StringEncoder(CharsetUtil.UTF_8))

                                    // 客户端响应处理器
                                    .addLast(new PoolResponseHandler())

                            ;
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    ;

            return bootstrap.connect(new InetSocketAddress(host, port));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "创建连接失败", e);
            throw e;
        }
    }

}
