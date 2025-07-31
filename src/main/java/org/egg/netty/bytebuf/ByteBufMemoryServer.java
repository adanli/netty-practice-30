package org.egg.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ResourceLeakDetector;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class ByteBufMemoryServer {
    private static final int MAX_FRAME_LENGTH = 1024 * 1024;
    private static final int DATA_SIZE = 1024 * 1024;

    // 内存分配统计
    private final AtomicLong heapAllocations = new AtomicLong();
    private final AtomicLong directAllocations = new AtomicLong();
    private final AtomicLong compositeAllocations = new AtomicLong();

    static {
        // 启动内存泄漏检测
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        System.setProperty("io.netty.leakDetection.targetRecords", "100");
    }

    public static void main(String[] args) {
        runPerformanceComparisons();
    }

    public static void runPerformanceComparisons() {
        System.out.println("\n========= 内存性能对比测试 =========");

        // 预热JVM
        testMemoryAllocationPerformance(100, false, false); // 预热堆内存
        testMemoryAllocationPerformance(100, true, false);  // 预热直接内存
        testMemoryAllocationPerformance(100, false, true);  // 预热池化堆内存
        testMemoryAllocationPerformance(100, true, true);   // 预热池化直接内存

        // 正式测试
        int testIterations = 1000;

        long heapTime = testMemoryAllocationPerformance(testIterations, false, false);
        long directTime = testMemoryAllocationPerformance(testIterations, true, false);
        long pooledHeapTime = testMemoryAllocationPerformance(testIterations, false, true);
        long pooledDirectTime = testMemoryAllocationPerformance(testIterations, true, true);

        System.out.printf("堆内存分配时间: %d ms (平均: %.3f ms/次)%n",
                         heapTime, heapTime / (double) testIterations);
        System.out.printf("直接内存分配时间: %d ms (平均: %.3f ms/次)%n",
                         directTime, directTime / (double) testIterations);
        System.out.printf("池化堆内存分配时间: %d ms (平均: %.3f ms/次)%n",
                         pooledHeapTime, pooledHeapTime / (double) testIterations);
        System.out.printf("池化直接内存分配时间: %d ms (平均: %.3f ms/次)%n",
                         pooledDirectTime, pooledDirectTime / (double) testIterations);

        System.out.println("========= 性能测试完成 =========\n");
    }

    private static long testMemoryAllocationPerformance(int iterations, boolean direct, boolean pooled) {
        ByteBufAllocator byteBufAllocator = pooled? PooledByteBufAllocator.DEFAULT:ByteBufAllocator.DEFAULT;
        byte[] bytes = new byte[DATA_SIZE];
        Random random = new Random();
        random.nextBytes(bytes);

        long start = System.currentTimeMillis();
        for (int i=0; i<iterations; i++) {

            ByteBuf buf = direct?byteBufAllocator.directBuffer(): byteBufAllocator.heapBuffer();
            try {
                buf.writeBytes(bytes);

                buf.readerIndex(0);
                while (buf.isReadable()) {
                    buf.readBytes(bytes);
                }

            } finally {
                ReferenceCountUtil.release(buf);
            }

        }

        return System.currentTimeMillis() - start;
    }

}
