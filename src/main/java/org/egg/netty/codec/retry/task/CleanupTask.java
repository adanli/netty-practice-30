package org.egg.netty.codec.retry.task;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.egg.netty.codec.retry.MessageRetryDemo;
import org.egg.netty.codec.retry.entity.PendingRequest;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.egg.netty.codec.retry.MessageRetryDemo.REQUEST_TIMEOUT_MS;

public class CleanupTask implements TimerTask {
    private final ChannelHandlerContext ctx;
    private final Map<String, PendingRequest> pendingRequests;
    private final Timer timer;

    public CleanupTask(ChannelHandlerContext ctx, Map<String, PendingRequest> pendingRequests, Timer timer) {
        this.ctx = ctx;
        this.pendingRequests = pendingRequests;
        this.timer = timer;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        long currentTime = System.currentTimeMillis();
        int removed = 0;

        // 清理超时过久的请求
        for (Map.Entry<String, PendingRequest> entry: pendingRequests.entrySet()) {
            PendingRequest request = entry.getValue();
            if (currentTime - request.getSentTime() > REQUEST_TIMEOUT_MS * (MessageRetryDemo.MAX_RETRIES + 1)) {
                pendingRequests.remove(entry.getKey());
                removed++;
                System.out.println("清理超时请求: " + entry.getKey());
            }

        }

        System.out.println("清理任务完成，移除 " + removed + " 个超时请求");

         // 重新调度清理任务
        timer.newTimeout(this, 30, TimeUnit.SECONDS);
    }
}
