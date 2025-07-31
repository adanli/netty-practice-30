package org.egg.netty.bytebuf;

import io.netty.buffer.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ResourceLeakDetector;

import java.util.Random;
import java.util.function.Supplier;
import java.util.logging.Logger;


public class ByteBufDemoServer {
    private static final Logger logger = Logger.getLogger(ByteBufDemoServer.class.getName());
//    private final static int PORT = 8088;
    private final static int DATA_SIZE = 1024*1024;
    private final static int TEST_ITERATIONS = 1000;
    private final static Random random = new Random();

    static {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
    }

    public static void main(String[] args) {
        // 1. 性能测试对比
        runPerformanceComparison();


    }

    public static void runPerformanceComparison() {
        logger.info("========= 内存性能对比测试 =========");

        // 测试1：堆内存分配与读写
        long heapAllocTime = testAllocation(() -> ByteBufAllocator.DEFAULT.heapBuffer(DATA_SIZE), TEST_ITERATIONS);
        logger.info(String.format("堆内存分配时间: %sms (平均: %sms/次)", heapAllocTime, heapAllocTime/(double)TEST_ITERATIONS));

        // 测试2：直接内存分配与读写
        long directAllocTime = testAllocation(() -> ByteBufAllocator.DEFAULT.directBuffer(DATA_SIZE), TEST_ITERATIONS);
        logger.info(String.format("直接内存分配时间: %sms (平均: %sms/次)", directAllocTime, directAllocTime/(double)TEST_ITERATIONS));

        // 测试3：池化堆内存分配与读写
        long pooledHeapAllocTime = testAllocation(() -> PooledByteBufAllocator.DEFAULT.heapBuffer(DATA_SIZE), TEST_ITERATIONS);
        logger.info(String.format("池化堆内存分配时间: %sms (平均: %sms/次)", pooledHeapAllocTime, pooledHeapAllocTime/(double)TEST_ITERATIONS));

        // 测试4：池化直接内存分配与读写
        long pooledDirectAllocTime = testAllocation(() -> PooledByteBufAllocator.DEFAULT.directBuffer(DATA_SIZE), TEST_ITERATIONS);
        logger.info(String.format("池化直接内存分配时间: %sms (平均: %sms/次)", pooledDirectAllocTime, pooledDirectAllocTime/(double)TEST_ITERATIONS));

        // 测试5: CompositeByteBuf性能
//        long compositeAllocTime = testCompositeByteBuf(TEST_ITERATIONS);
//        logger.info(String.format("合并内存分配时间: %sms (平均: %sms/次)", compositeAllocTime, compositeAllocTime/(double)TEST_ITERATIONS));

    }

    public static long testAllocation(Supplier<ByteBuf> bufSuppliers, int iterations) {
        byte[] bytes = new byte[DATA_SIZE];
        random.nextBytes(bytes);

        long start = System.currentTimeMillis();
        for (int i=0; i<iterations; i++) {
            ByteBuf buf = null;

            try {
                buf = bufSuppliers.get();
                if(buf == null) continue;

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

    private static long testCompositeByteBuf(int iterations) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            CompositeByteBuf compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
            try {
                // 添加多个缓冲区
                for (int j=0; j<10; j++) {
                    ByteBuf part = Unpooled.buffer(DATA_SIZE/10);
                    byte[] bytes = new byte[DATA_SIZE/10];
                    random.nextBytes(bytes);
                    part.writeBytes(bytes);
                    compositeByteBuf.addComponent(true, part);
                }


                compositeByteBuf.readerIndex(0);
                while (compositeByteBuf.isReadable()) {
                    compositeByteBuf.readableBytes();
                }

            } finally {
//                compositeByteBuf.release();
                ReferenceCountUtil.release(compositeByteBuf);
            }

        }

        return System.currentTimeMillis() - start;
    }


}
