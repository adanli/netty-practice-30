package org.egg.netty.file.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.stream.ChunkedStream;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class FileServerHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger LOGGER = Logger.getLogger(FileServerHandler.class.getName());
    private final int chunkSize = 60*1024;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        File file = new File(msg);
        if(!file.exists()) {
            System.err.println("文件不存在: " + msg);
        }

        ctx.writeAndFlush(new ChunkedStream(Files.newInputStream(Path.of(msg)), chunkSize))
                .addListener(ChannelFutureListener.CLOSE)
        ;

    }

}
