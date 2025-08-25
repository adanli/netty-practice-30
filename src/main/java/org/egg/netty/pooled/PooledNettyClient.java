package org.egg.netty.pooled;

import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PooledNettyClient {
    private static String host = "localhost";
    private static int port = 8089;
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static PooledNettyConnectionPool pool = new PooledNettyConnectionPool(host, port, 3, 10, 3000);

    public static void main(String[] args) throws Exception{


        PooledNettyClient client = new PooledNettyClient();

        // 等待预热
        Thread.sleep(5000);
        System.out.println("服务端预热完成");
        pool.status();

        for (int i = 0; i < 200; i++) {
            final int id = i;
            executorService.submit(() -> {
                String msg = String.format("Request-%s", id);
                client.sendMessage(msg);
            });

        }


        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);

    }

    private void sendMessage(String msg) {
        Channel channel = pool.borrowChannel();
        try {
            channel.writeAndFlush(String.format("[%s]: %s", channel.id(), msg));

            Thread.sleep(500);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        } finally {
            pool.returnChannel(channel);
        }
    }

}
