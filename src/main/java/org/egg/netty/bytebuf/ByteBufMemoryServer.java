package org.egg.netty.bytebuf;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ResourceLeakDetector;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class ByteBufMemoryServer {
    private static final int MAX_FRAME_LENGTH = 1024 * 1024;
    private static final int DATA_SIZE = 1024 * 1024;
    private static final int PORT = 8088;

    // 内存分配统计
    private static final AtomicLong heapAllocations = new AtomicLong();
    private static final AtomicLong directAllocations = new AtomicLong();
    private static final AtomicLong compositeAllocations = new AtomicLong();

    static {
        // 启动内存泄漏检测
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        System.setProperty("io.netty.leakDetection.targetRecords", "100");
    }

    public static void main(String[] args) throws Exception {
        runPerformanceComparisons();

        startServer();
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
        long compositeTime = testCompositeBufferPerformance(testIterations);

        System.out.printf("堆内存分配时间: %d ms (平均: %.3f ms/次)%n",
                         heapTime, heapTime / (double) testIterations);
        System.out.printf("直接内存分配时间: %d ms (平均: %.3f ms/次)%n",
                         directTime, directTime / (double) testIterations);
        System.out.printf("池化堆内存分配时间: %d ms (平均: %.3f ms/次)%n",
                         pooledHeapTime, pooledHeapTime / (double) testIterations);
        System.out.printf("池化直接内存分配时间: %d ms (平均: %.3f ms/次)%n",
                         pooledDirectTime, pooledDirectTime / (double) testIterations);
        System.out.printf("CompositeByteBuf 组装时间: %d ms (平均: %.3f ms/次)%n",
                         compositeTime, compositeTime / (double) testIterations);

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

    private static long testCompositeBufferPerformance(int iterations) {
        ByteBufAllocator byteBufAllocator = UnpooledByteBufAllocator.DEFAULT;
        Random random = new Random();
        // 随机生成10组数据
        byte[][] randomDatas = new byte[10][DATA_SIZE];
        for (int i=0; i<10; i++) {
            random.nextBytes(randomDatas[i]);
        }

        long start = System.currentTimeMillis();
        for (int i=0; i<iterations; i++) {
            CompositeByteBuf compositeByteBuf = byteBufAllocator.compositeBuffer();

            try {
                // 10个组
                for (int j=0; j<10; j++) {
                    ByteBuf buf = Unpooled.buffer();
                    buf.writeBytes(randomDatas[j]);
                    compositeByteBuf.addComponent(true, buf);
                }

                compositeByteBuf.readerIndex(0);
                if(compositeByteBuf.isReadable()) {
                    byte[] bytes = new byte[10*DATA_SIZE];
                    compositeByteBuf.readBytes(bytes);
                }

            } finally {
                ReferenceCountUtil.release(compositeByteBuf);
            }

        }

        return System.currentTimeMillis() - start;

    }

    public static void startServer() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                // 解决粘包断包问题
                                .addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 0, 4, 0 ,4))
                                .addLast(new LengthFieldPrepender(4))
                                // 业务处理器
                                .addLast(new MemoryDemoHandler(heapAllocations, directAllocations, compositeAllocations))
                            ;

                        }
                    })
                    .option(ChannelOption.SO_BACKLOG,  100)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    ;

            ChannelFuture cf = serverBootstrap.bind(PORT).sync();
            System.out.println("ByteBuf内存管理服务已启动, 端口: " + PORT);

            cf.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }


    }

}
