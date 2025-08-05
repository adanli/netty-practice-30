package org.egg.netty.ssl.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.io.FileOutputStream;

public class FileClientHandler extends SimpleChannelInboundHandler<HttpObject> {
    private FileOutputStream out;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if(msg instanceof HttpResponse response) {
            if(response.status() != HttpResponseStatus.OK) {
                System.err.println("下载失败: " + response.status());
                ctx.close();
                return;
            }

            out = new FileOutputStream("downloaded-secret.txt");
        }

        if(msg instanceof HttpContent content) {
            content.content().readBytes(out, content.content().readableBytes());
            if(content instanceof LastHttpContent) {
                out.close();
                System.out.println("文件下载完成!");
                ctx.close();
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("客户端异常");
        ctx.close();
    }
}
