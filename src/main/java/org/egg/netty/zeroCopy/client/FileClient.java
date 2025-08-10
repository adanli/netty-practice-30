package org.egg.netty.zeroCopy.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

public class FileClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8088;

    public static void main(String[] args) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        String mode = "tranditional";

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new HttpClientCodec())
                                    .addLast(new HttpObjectAggregator(300*1024*1024))

                                    .addLast(new ChunkedWriteHandler())

                                    .addLast(new SimpleChannelInboundHandler<HttpObject>() {
                                        private OutputStream outputStream;
                                        private long startTime;
                                        private String savePath = "D:\\code\\java\\practice\\netty-practice-30\\src\\main\\resources\\20230801-083745.csv";

                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            startTime = System.nanoTime();
                                        }

                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, HttpObject object) throws Exception {
                                            if(object instanceof HttpResponse response) {
                                                if(response.status().code() != 200) {
                                                    System.err.println("下载失败");
                                                    ctx.close();
                                                    return;
                                                }

                                                try {
                                                    outputStream = new FileOutputStream(savePath);
                                                } catch (Exception e) {
                                                    System.err.println("文件创建失败");
                                                    ctx.close();
                                                    return;
                                                }

                                                if(object instanceof HttpContent content) {
                                                    ByteBuf buf = content.content();

                                                    try {
                                                        byte[] bytes = new byte[buf.readableBytes()];
                                                        buf.readBytes(bytes, 0, buf.readableBytes());
                                                        outputStream.write(bytes);

                                                    } catch (Exception e) {
                                                        System.err.println("文件写入失败");
                                                    }


                                                }

                                                if(object instanceof LastHttpContent) {
                                                    try {
                                                        outputStream.close();
                                                        long duration = System.nanoTime() - startTime;
                                                        System.out.printf("文件下载完成, 耗时: %.2f ms%n", duration/1_1000_1000.0);
                                                    } catch (Exception e) {
                                                        System.err.println("文件关闭失败");
                                                    }
                                                    ctx.close();
                                                }


                                            }
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                            System.err.println("客户端处理异常: " + cause);
                                            ctx.close();
                                        }
                                    })
                            
                            ;
                        }
                    })

                    ;

            Channel channel = bootstrap.connect(SERVER_HOST, SERVER_PORT).sync().channel();
            URI uri = new URI("http://localhost:" + SERVER_PORT + "/20230801-083745.csv" + "?mode="+mode);
            FullHttpRequest  request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getPath());
            request.headers().set(HttpHeaderNames.HOST, SERVER_HOST);

            channel.writeAndFlush(request);

            // 等待下载完成
            channel.closeFuture().sync();


        } finally {
            group.shutdownGracefully().sync();
        }

    }
}
