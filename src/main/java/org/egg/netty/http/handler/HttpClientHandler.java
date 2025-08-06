package org.egg.netty.http.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.io.FileOutputStream;


public class HttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private final String dicPath;

    public HttpClientHandler(String dicPath) {
        this.dicPath = dicPath;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        if(msg.getStatus() != HttpResponseStatus.OK) {
            System.err.println("服务端响应异常: " + msg.getStatus());
        }

        ByteBuf buf = msg.content();
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes, 0, buf.readableBytes());

        try (FileOutputStream fos = new FileOutputStream(dicPath+"\\new-file.txt")){
            fos.write(bytes);
        }
        System.out.println("客户端下载文件完成");

    }

}
