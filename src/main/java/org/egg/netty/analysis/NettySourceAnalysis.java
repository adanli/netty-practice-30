package org.egg.netty.analysis;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.openjdk.jol.vm.VM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;

/**
 * 关注EventLoop的调度和Pipeline的初始化
 */
public class NettySourceAnalysis {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettySourceAnalysis.class);

    public static void main(String[] args) throws Exception{
        LOGGER.info("Java VM: {}", VM.current().details());
        LOGGER.info("Process Id: {}", ManagementFactory.getRuntimeMXBean().getName());

        // 创建EventLoop
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                    pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                }
            });

            ChannelFuture cf = bootstrap.bind(8090).sync();
            cf.addListener(listener -> {
                if(listener.isSuccess()) {
                    LOGGER.info("服务端启动, 注册端口8090");
                    LOGGER.info("设置断点分析");
                    LOGGER.info("1. EventLoopGroup初始化完成");
                    LOGGER.info("2. ChannelPipeline初始化完成");
                    LOGGER.info("3. EventLoop开始调度");

                }
            });

            cf.channel().closeFuture().channel();

        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }


    }

}
