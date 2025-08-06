package org.egg.netty.ssl.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.file.Files;

public class FileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final File file;

    public FileServerHandler(File file) {
        this.file = file;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if(!request.getDecoderResult().isSuccess()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if(request.getMethod() != HttpMethod.GET) {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        // 发送文件
        RandomAccessFile raf;

        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException fileNotFoundException) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
//        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, raf.length());

        ctx.write(response);
        ctx.write(new ChunkedFile(raf), ctx.newProgressivePromise());
//        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);

        System.out.println("服务端完成回写");

        /*ctx.writeAndFlush(new ChunkedStream(Files.newInputStream(file.toPath()), 60*1024))
                .addListener(ChannelFutureListener.CLOSE)
        ;*/


        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
//                .addListener(ChannelFutureListener.CLOSE)
                .addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        System.out.println("文件传输完成");
                        future.channel().close();
                    }
                })
//        ctx.writeAndFlush(response);
        ;

    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

}
