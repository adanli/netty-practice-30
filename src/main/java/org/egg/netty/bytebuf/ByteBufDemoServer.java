package org.egg.netty.bytebuf;

import io.netty.util.ResourceLeakDetector;


public class ByteBufDemoServer {
//    private final Logger logger = Logger.getLogger(ByteBufDemoServer.class.getName());
//    private final static int PORT = 8088;
//    private final static int DATA_SIZE = 1024*1024;
//    private final static int TEST_ITERATIONS = 1024*1024;
//    private final static Random random = new Random();

    static {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
    }

    public static void main(String[] args) {
        // 1. 性能测试对比



    }


}
