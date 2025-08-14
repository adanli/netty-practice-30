package org.egg.netty.pool;

import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PooledNettyClient {
    private final NettyConnectionPool connectionPool;

    public PooledNettyClient(String host, int port) {
        this.connectionPool = new NettyConnectionPool(host, port, 10, 3, 30_100);
    }

    public static void main(String[] args) throws Exception{
        PooledNettyClient client = new PooledNettyClient("localhost", 8080);
        ExecutorService executor = Executors.newFixedThreadPool(20);

        // 模拟并发请求
        for (int i = 0; i < 100; i++) {
            final int requestId = i;
            executor.submit(() -> {
                client.sendRequest("Request-" + requestId);
                System.out.println("Sent request: " + requestId);
            });
            Thread.sleep(50); // 控制请求速率
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // 关闭客户端
        client.shutdown();
    }

    public void sendRequest(String message) {
        Channel channel = null;
        try {
            // 1. 从连接池获取连接
            channel = connectionPool.borrowConnection();

            // 2. 发送请求
            channel.writeAndFlush(message).addListener(future -> {
                if (!future.isSuccess()) {
                    System.err.println("Send failed: " + future.cause().getMessage());
                }
            });

            // 3. 在实际应用中，这里可能会等待响应
            // 本示例中立即归还连接（在实际异步系统中，应在收到响应后归还）
            Thread.sleep(100); // 模拟处理时间

        } catch (Exception e) {
            System.err.println("Request failed: " + e.getMessage());
        } finally {
            // 4. 归还连接
            if (channel != null) {
                connectionPool.returnConnection(channel);
            }
        }
    }

    public void shutdown() {
        connectionPool.shutdown();
    }

}
