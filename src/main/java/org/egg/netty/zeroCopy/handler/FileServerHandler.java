package org.egg.netty.zeroCopy.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

public class FileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final String baseDir;

    public FileServerHandler(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if(!request.method().equals(HttpMethod.GET)) {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        String path = request.uri();
        if(path == null) {
            System.err.println("错误的请求类型");
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        File file = new File(baseDir, path);
        if(!file.exists() || file.isDirectory()) {
            System.err.println("文件不存在");
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        // 获取传输模式
        String mode = this.getMode(request);
        long startTime = System.currentTimeMillis();

        try {
            if(mode.equals("traditional")) {
                sendWithTraditionalIO(ctx, file);
            } else {
                // zero-copy
                sendWithZeroCopy(ctx, file);
            }

            long duration = System.currentTimeMillis() - startTime;
            System.out.printf("文件传输完成: %s | 模式: %s | 耗时: %.2f ms%n", file.getName(), mode, duration/1_1000_1000.0);

        } catch (Exception e) {
            System.err.println("传输文件失败" + e);
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }


    }


    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        ctx.writeAndFlush(status);
    }

    private String getMode(FullHttpRequest request) {
        // 从查询参数中获取
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        return decoder.parameters().getOrDefault("mode", List.of("zero-copy")).get(0);
//        return decoder.parameters().getOrDefault("mode", List.of("traditional")).get(0);
    }

    private void sendWithZeroCopy(ChannelHandlerContext ctx, File file) throws Exception{
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        long length = raf.length();

        // 创建响应
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers()
                .set(HttpHeaderNames.CONTENT_LENGTH, length)
                .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_OCTET_STREAM)
                .set("X-Transformer-Mode", "zero-copy")
                ;
        ctx.write(response);

        // 使用FileRegion零拷贝传输文件内容
        FileRegion region = new DefaultFileRegion(raf.getChannel(), 0, length);
        ChannelFuture transferFuture = ctx.write(region, ctx.newProgressivePromise());
        transferFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture channelProgressiveFuture, long progress, long total) throws Exception {
//                System.out.printf("传输进度: %s / %s%n", progress, total);
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                System.out.println("传输完成");
            }
        });
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
            .addListener(ChannelFutureListener.CLOSE)
        ;


    }

    private void sendWithTraditionalIO(ChannelHandlerContext ctx, File file) throws Exception{
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        long length = raf.length();

        // 创建响应
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers()
                .set(HttpHeaderNames.CONTENT_LENGTH, length)
                .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_OCTET_STREAM)
                .set("X-Transformer-Mode", "traditional")
        ;
        ctx.write(response);

        // 使用ChunkFile零拷贝传输文件内容
        ChunkedFile chunkedFile = new ChunkedFile(raf);
        ChannelFuture transferFuture = ctx.write(chunkedFile, ctx.newProgressivePromise());
        transferFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture channelProgressiveFuture, long progress, long total) throws Exception {
//                System.out.printf("传输进度: %s / %s%n", progress, total);
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                System.out.println("传输完成");
            }
        });
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
                .addListener(ChannelFutureListener.CLOSE)
        ;

    }


}
