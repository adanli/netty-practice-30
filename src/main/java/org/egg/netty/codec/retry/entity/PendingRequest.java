package org.egg.netty.codec.retry.entity;

import io.netty.util.Timeout;
import org.egg.netty.codec.retry.task.TimeoutTask;

public class PendingRequest {
    private final BusinessRequest request;
    private final long sentTime;
    private final int retryCount;
    private final Timeout timeout;
    private final TimeoutTask timeoutTask;

    public TimeoutTask getTimeoutTask() {
        return timeoutTask;
    }

    public PendingRequest(BusinessRequest request, long sentTime, int retryCount, Timeout timeout, TimeoutTask timeoutTask) {
        this.request = request;
        this.sentTime = sentTime;
        this.retryCount = retryCount;
        this.timeout = timeout;
        this.timeoutTask = timeoutTask;
    }

    public BusinessRequest getRequest() {
        return request;
    }

    public long getSentTime() {
        return sentTime;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Timeout getTimeout() {
        return timeout;
    }
}
