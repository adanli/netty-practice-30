package org.egg.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MemoryDemoHandler extends ChannelInboundHandlerAdapter {
    // 20%概率泄露
    private static final int LEAK_PROBABILITY = 20;
    private final AtomicLong heapAllocations;
    private final AtomicLong directAllocations;
    private final AtomicLong compositeAllocations;

    private final Logger logger = Logger.getLogger(MemoryDemoHandler.class.getName());

    public MemoryDemoHandler(AtomicLong heapAllocations, AtomicLong directAllocations, AtomicLong compositeAllocations) {
        this.heapAllocations = heapAllocations;
        this.directAllocations = directAllocations;
        this.compositeAllocations = compositeAllocations;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(!(msg instanceof ByteBuf buf)) {
            ReferenceCountUtil.release(msg);
            return;
        }

        try {
            // 1. 记录内存分配类型
            recordAllocationType(buf);

            // 2. 处理客户端请求
            String request = processRequest(buf);

            // 3. 创建响应（使用不同的内存类型）
            ByteBuf response = createResponse(ctx.alloc(), request);

            // 发送响应
            ctx.writeAndFlush(response)
                    .addListener(f -> {
                        if(!f.isSuccess()) {
                            System.err.println("响应发送失败: " + f.cause());
                            response.release();
                        }
                    });


        } finally {
            // 5. 释放接收到的缓冲区
            ReferenceCountUtil.release(buf);
        }

    }

    private void recordAllocationType(ByteBuf buf) {
        if(buf instanceof CompositeByteBuf) {
            compositeAllocations.incrementAndGet();
        } else if(buf.hasMemoryAddress()) {
            directAllocations.incrementAndGet();
        } else {
            heapAllocations.incrementAndGet();
        }

        long total = compositeAllocations.get() + directAllocations.get() + heapAllocations.get();

//        if(true) {
        if(total%100 == 0) {
            System.out.printf("内存分配统计: 堆内存=%d, 直接内存=%d, 组合内存=%d%n",
                                 heapAllocations.get(), directAllocations.get(), compositeAllocations.get());
        }

    }

    private String processRequest(ByteBuf buf) {
        // 读取请求内容
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return new String(bytes, Charset.defaultCharset());
    }

    private ByteBuf createResponse(ByteBufAllocator allocator, String request) {
        // 随机选择响应类型
        int responseType = new Random().nextInt(100);

        // 20%概率发生内存泄漏
        if(responseType < LEAK_PROBABILITY) {
            System.err.println("===== 创造内存泄露 =====");
            ByteBuf leakedBuf = createLeakedBuffer(allocator);
            System.err.printf("已泄漏 %d 字节的缓冲区 (引用计数: %d)%n",
                                 leakedBuf.readableBytes(), leakedBuf.refCnt());
            return allocator.buffer().writeBytes("内存泄漏已创建".getBytes());
        }

        // 创建正常的响应
        switch (responseType % 4) {
            case 0:
                // 堆内存响应
                return allocator.heapBuffer().writeBytes(String.format("堆内存响应: %s", request).getBytes(Charset.defaultCharset()));
            case 1:
            // 直接内存响应
            return allocator.directBuffer().writeBytes(String.format("直接内存响应: %s", request).getBytes(Charset.defaultCharset()));
            case 2:
            // 组合内存响应
            CompositeByteBuf compositeByteBuf = allocator.compositeBuffer();
            compositeByteBuf.addComponent(true, allocator.buffer().writeBytes("响应组合[1]: ".getBytes(Charset.defaultCharset())));
            compositeByteBuf.addComponent(true, allocator.buffer().writeBytes(request.getBytes(Charset.defaultCharset())));
            compositeByteBuf.addComponent(true, allocator.buffer().writeBytes(" [2]".getBytes(Charset.defaultCharset())));
            return compositeByteBuf;
            case 3:
            // 引用计数演示
            return demonstrateReferenceCounting(allocator, request);
            default:
                return allocator.buffer().writeBytes(String.format("默认响应: %s", request).getBytes(Charset.defaultCharset()));
        }


    }

    private ByteBuf createLeakedBuffer(ByteBufAllocator allocator) {
        // 创建缓冲区但不释放
        ByteBuf leakedBuf = allocator.directBuffer(1024);
        leakedBuf.writeBytes("这个缓冲区会被泄漏".getBytes());
        return leakedBuf;
    }

    private ByteBuf demonstrateReferenceCounting(ByteBufAllocator allocator, String request) {
        ByteBuf buf = allocator.directBuffer();
        buf.writeBytes(("引用计数演示: " + request).getBytes());
        System.out.println("初始化引用计数: " + buf.refCnt());

        // 增加引用计数
        buf.retain();
        System.out.println("retain() 后引用计数: " + buf.refCnt());

        // 传递引用 (接收方应负责释放)
        ByteBuf wrappedBuf = buf.retain();
        buf.release();
        System.out.println("release() 后引用计数: " + buf.refCnt());

        return wrappedBuf;

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("处理错误: " + cause.getMessage());
        logger.log(Level.WARNING, SimpleClientHandler.class.getName() + "发生错误", cause);
        ctx.close();
    }

}
