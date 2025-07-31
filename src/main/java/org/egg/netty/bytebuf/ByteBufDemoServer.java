package org.egg.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ResourceLeakDetector;

import java.util.Random;
import java.util.function.Supplier;
import java.util.logging.Logger;


public class ByteBufDemoServer {
    private static final Logger logger = Logger.getLogger(ByteBufDemoServer.class.getName());
//    private final static int PORT = 8088;
    private final static int DATA_SIZE = 1024*10;
    private final static int TEST_ITERATIONS = 10;
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
        long heapAllocTime = testAllocation(() -> ByteBufAllocator.DEFAULT.heapBuffer(TEST_ITERATIONS*DATA_SIZE), TEST_ITERATIONS);
        logger.info(String.format("堆内存分配时间: %sms (平均: %sms/次)", heapAllocTime, heapAllocTime/(double)TEST_ITERATIONS));

        // 测试2：直接内存分配与读写
        long directAllocTime = testAllocation(() -> ByteBufAllocator.DEFAULT.directBuffer(TEST_ITERATIONS*DATA_SIZE), TEST_ITERATIONS);
        logger.info(String.format("直接内存分配时间: %sms (平均: %sms/次)", directAllocTime, directAllocTime/(double)TEST_ITERATIONS));

        // 测试3：池化堆内存分配与读写
        long pooledHeapAllocTime = testAllocation(() -> PooledByteBufAllocator.DEFAULT.heapBuffer(TEST_ITERATIONS*DATA_SIZE), TEST_ITERATIONS);
        logger.info(String.format("池化堆内存分配时间: %sms (平均: %sms/次)", pooledHeapAllocTime, pooledHeapAllocTime/(double)TEST_ITERATIONS));

        // 测试4：池化直接内存分配与读写
        long pooledDirectAllocTime = testAllocation(() -> PooledByteBufAllocator.DEFAULT.directBuffer(TEST_ITERATIONS*DATA_SIZE), TEST_ITERATIONS);
        logger.info(String.format("池化直接内存分配时间: %sms (平均: %sms/次)", pooledDirectAllocTime, pooledDirectAllocTime/(double)TEST_ITERATIONS));


    }

    /*public static long testAllocation(ByteBuf buf, int iterations) {
        long start = System.currentTimeMillis();
        for (int i=0; i<iterations; i++) {
            try {
                    // 写入数据
                    for(int j=0; j<DATA_SIZE; j++) {
                        buf.writeByte(random.nextInt(256));
                    }

                    // 读取数据
                    buf.readerIndex(0);
                    while (buf.isReadable()) {
                        buf.readByte();
                    }
    //                System.out.println("第"+i+"次");
            } finally {
                // 释放缓冲区
                ReferenceCountUtil.release(buf);
            }
        }


        return System.currentTimeMillis() - start;
    }*/

    public static long testAllocation(Supplier<ByteBuf> bufSuppliers, int iterations) {
        long start = System.currentTimeMillis();

        for (int i=0; i<iterations; i++) {
            ByteBuf buf = null;

            try {
                buf = bufSuppliers.get();
                if(buf == null) continue;

                byte[] bytes = new byte[DATA_SIZE];
                random.nextBytes(bytes);
                for (int j=0; j<DATA_SIZE; j++) {
//                    buf.writeByte(random.nextInt(256));
                    buf.writeBytes(bytes);

                    buf.readerIndex(0);
                    while (buf.isReadable()) {
//                        buf.readByte();
                        buf.readBytes(bytes);
                    }
                }

            } finally {
                ReferenceCountUtil.release(buf);
            }


        }

        return System.currentTimeMillis() - start;

    }


}
