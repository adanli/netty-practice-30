package org.egg.netty.file.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class FileClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final String savePath;
    private final String destFile = "receive.txt";

    public FileClientHandler(String savePath) {
        this.savePath = savePath;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes, 0, msg.readableBytes());

        try (OutputStream outputStream = new FileOutputStream(savePath + "/" + destFile)){
            outputStream.write(bytes);
            outputStream.flush();
            System.out.println("客户端完成文件下载到本地");
        }
    }


}
