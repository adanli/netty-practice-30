package org.egg.netty.codec.retry.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.egg.netty.codec.retry.entity.BusinessResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ClientBusinessHandler extends ChannelInboundHandlerAdapter {
    private final Map<String, Long> pendingRequests = new ConcurrentHashMap<>();

    // 定时器
    private final Timer timer = new HashedWheelTimer(
            new DefaultThreadFactory("retry-timer"),
            100, TimeUnit.MILLISECONDS
    );

    // 序列号生成器（用于日志显示）
    private final AtomicLong requestCounter = new AtomicLong(1);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof BusinessResponse response) {
            String seqId = response.getSequenceId();

            // 1. 从等待队列中移除
            pendingRequests.remove(seqId);
            // 2. 取消超时检测任务


        }
    }
}
