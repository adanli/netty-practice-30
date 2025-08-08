package org.egg.netty.httpfile.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;

import java.io.File;
import java.io.RandomAccessFile;

public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final String DIR_PATH = "D:\\code\\java\\practice\\netty-practice-30\\src\\main\\resources";
    private RandomAccessFile raf;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        if(!msg.getDecoderResult().isSuccess()) {
            this.sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if(msg.getMethod() != HttpMethod.GET) {
           this.sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
           return;
        }

        String fileName = msg.uri();

        String path = String.format("%s%s", DIR_PATH, fileName);

        File file = new File(path);
        if(!file.exists() || !file.isFile()) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        raf = new RandomAccessFile(path, "r");


        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers()
                .set(HttpHeaderNames.CONTENT_LENGTH, raf.length())
                .set(HttpHeaderNames.CONTENT_TYPE,  "application/octet-stream")
                .set(HttpHeaderNames.CONNECTION,  HttpHeaderValues.KEEP_ALIVE)
                ;

        ctx.write(response);
        ctx.write(new ChunkedFile(raf, 0, raf.length(), 8192), ctx.newProgressivePromise()).addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture channelProgressiveFuture, long progress, long total) throws Exception {
                if(total > 0)
                    System.out.printf("下载进度: %d / %d%n", progress, total);
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture channelProgressiveFuture) throws Exception {
                if(channelProgressiveFuture.isSuccess()) {
                    System.out.println("下载完成");
                }
            }
        });

        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
                .addListener(ChannelFutureListener.CLOSE);

    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        ctx.writeAndFlush(new DefaultHttpResponse(
                HttpVersion.HTTP_1_1, status
        ));
    }

}
