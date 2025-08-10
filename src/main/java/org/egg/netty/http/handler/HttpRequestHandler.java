package org.egg.netty.http.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;

import java.io.RandomAccessFile;


public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final String filePath;
    private RandomAccessFile raf;

    public HttpRequestHandler(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        if(!msg.getDecoderResult().isSuccess()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }


        // 读取文件
        raf = new RandomAccessFile(filePath, "r");

        // 创建响应
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers()
                        .set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream")
                        .set(HttpHeaderNames.CONTENT_LENGTH, raf.length())
        ;

        ctx.write(response);
        ctx.write(new ChunkedFile(raf), ctx.newProgressivePromise());
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
         .addListener((ChannelFutureListener) future -> {
             System.out.println("文件传输完成");
//             future.channel().close();
         });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        ctx.writeAndFlush(
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, ctx.alloc().buffer(0))
                )
                .addListener(ChannelFutureListener.CLOSE);
    }
}
