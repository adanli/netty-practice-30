package org.egg.netty.codec.retry.task;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.egg.netty.codec.retry.MessageRetryDemo;
import org.egg.netty.codec.retry.entity.BusinessRequest;
import org.egg.netty.codec.retry.entity.PendingRequest;

import java.util.Map;

/**
 * 超时任务
 */
public class TimeoutTask implements TimerTask {
    private final ChannelHandlerContext ctx;
    private final BusinessRequest request;
    private final int retryCount;
    private final Map<String, PendingRequest> processRequests;

    public TimeoutTask(ChannelHandlerContext ctx, BusinessRequest request, int retryCount, Map<String, PendingRequest> processRequests) {
        this.ctx = ctx;
        this.request = request;
        this.retryCount = retryCount;
        this.processRequests = processRequests;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        // 检查是否在等待队列中
        if(processRequests.containsKey(request.getSequenceId())) {
            System.out.printf("请求超时 [seqId=%s, retry=%s]%n", request.getSequenceId(), retryCount);

            // 重试发送
            if(retryCount < MessageRetryDemo.MAX_RETRIES) {
                System.out.printf("重新发送请求 [seqId=%s, retry=%s%n]", request.getSequenceId(), retryCount);

//                sendRequest(ctx, request, retryCount+1);

            } else {
                System.out.printf("达到最大重试次数，放弃重试, seqId=%s%n", request.getSequenceId());
                processRequests.remove(request.getSequenceId());
            }

        }


    }
}
