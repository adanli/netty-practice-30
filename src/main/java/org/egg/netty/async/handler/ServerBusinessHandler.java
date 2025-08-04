package org.egg.netty.async.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Promise;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 模拟数据库10s
 */
public class ServerBusinessHandler extends ChannelInboundHandlerAdapter {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Promise<String> promise = ctx.executor().newPromise();

        try {
            if(msg instanceof String s) {
                System.out.printf("[%s]服务端接收到请求: %s%n", sdf.format(new Date()), s);

                if(s.startsWith("BLOCK: ")) {
                    this.handleWithBlocking(promise);

                } else if(s.startsWith("CALLBACK: ")) {
                    this.handleWithCallback(promise);
                } else {
                    System.err.println("错误的请求格式");
                }

                promise.addListener(listener -> {
                   if(listener.isSuccess()) {
                       ctx.writeAndFlush(listener.getNow());
                   } else {
                       ctx.writeAndFlush("查询失败: " + listener.cause().getMessage());
                   }
                });

            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void handleWithBlocking(Promise<String> promise) {
        Future<String> future = EXECUTOR_SERVICE.submit(this::queryDatabase);

        // 同步阻塞等待
        try {
            String result = future.get(10, TimeUnit.SECONDS);
            promise.setSuccess("[阻塞]: " + result);
            System.out.printf("[%s]服务端返回阻塞请求: %s%n", sdf.format(new Date()), result);
        } catch (Exception e) {
            promise.setFailure(e);
            System.err.printf("[%s]服务端返回阻塞请求失败%n", sdf.format(new Date()));
        }

    }

    private void handleWithCallback(Promise<String> promise) {
        EXECUTOR_SERVICE.execute(() -> {
            try {
                String result = queryDatabase();
                promise.setSuccess("[回调]: " + result);
                System.out.printf("[%s]服务端返回回调请求: %s%n", sdf.format(new Date()), result);
            } catch (Exception e) {
                promise.setFailure(e);
            }

        });
    }

    private String queryDatabase(){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.printf("[%s]服务端查询数据完成%n", sdf.format(new Date()));
        return "search from database: hello";
    }

}
