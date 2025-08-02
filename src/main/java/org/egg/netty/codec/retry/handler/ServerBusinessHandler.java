package org.egg.netty.codec.retry.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.egg.netty.codec.retry.MessageRetryDemo;
import org.egg.netty.codec.retry.entity.BusinessRequest;
import org.egg.netty.codec.retry.entity.BusinessResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerBusinessHandler extends ChannelInboundHandlerAdapter {
    // 检测重复请求
    private final Map<String, Long> processesRequests = new ConcurrentHashMap();
    // 重复检测窗口
    private static final long DUPLICATE_WINDOWS_MS = 60*1000;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof BusinessRequest request) {
                // 1. 检查是否重复请求
                if(isDuplicateRequest(request.getSequenceId())) {
                    System.out.println("检测到重复请求: " + request.getSequenceId() + ", 忽略处理");
                    return;
                }

                // 2. 处理业务请求
                System.out.println("服务端处理请求: " + request);

                boolean success = Math.random() > 0.3;
                int status = success?200:500;
                String result = success?"处理成功: " + request.getContent().toUpperCase():"处理失败: " + request.getContent();

                // 3. 发送响应
                BusinessResponse response = new BusinessResponse(result, status);
                response.setSequenceId(request.getSequenceId());
                ctx.writeAndFlush(response);

            }

        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    private boolean isDuplicateRequest(String sequenceId) {
        long currentTime = System.currentTimeMillis();

        Long existTime = processesRequests.putIfAbsent(sequenceId, currentTime);
        if(existTime != null) {
            return true;
        }

        // 定期清理旧记录
        if(processesRequests.size() > 1000)  {
            this.cleanupOldRequests(currentTime);
        }
        return false;
    }

    private void cleanupOldRequests(long currentTime) {
        processesRequests.entrySet().removeIf(entry ->
            currentTime - entry.getValue() > DUPLICATE_WINDOWS_MS
        );
    }
}
