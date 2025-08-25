package org.egg.netty.pooled;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * 负责管理连接池，提供连接申请、收回的功能
 * 连接：Channel
 */
public class PooledNettyConnectionPool {
    private static final Logger LOGGER = Logger.getLogger(PooledNettyConnectionPool.class.getName());
    /**
     * 最近更新时间
     */
    private final AttributeKey<Long> LAST_USED_TIME = AttributeKey.valueOf("lastUsedTime");

    /**
     * 记录最大连接数量
     */
    private final int maxConnections;
    /**
     * 记录最小连接数量
     */
    private final int minConnections;
    /**
     * 记录总共的连接
     */
    private AtomicInteger totalConnections;
    /**
     * 记录活跃的连接
     */
    private Set<Channel> activeConnections;
    /**
     * 记录空闲的连接
     */
    private BlockingQueue<Channel> idleConnections;
    /**
     * 服务端的host和port
     */
    private final String host;
    private final int port;

    private final PooledConnectionFactory factory;

    /**
     * 最大超时时间
     */
    private int maxIdleTime;

    private ScheduledExecutorService executorService;

    public PooledNettyConnectionPool(String host, int port, int minConnections, int maxConnections, int maxIdleTime) {
        this.host = host;
        this.port = port;
        this.minConnections = minConnections;
        this.maxConnections = maxConnections;

        this.idleConnections = new ArrayBlockingQueue<>(maxConnections);
        this.activeConnections = new HashSet<>();
        this.totalConnections = new AtomicInteger(0);

        this.maxIdleTime = maxIdleTime;

        this.factory = new PooledConnectionFactory(this.host, this.port);

        this.healthCheck();

        initPool();
    }

    /**
     * 1. 初始化连接池（填充）
     */
    public void initPool() {
        for (int i = 0; i < minConnections; i++) {
            createChannel();
        }
    }

    /**
     * 2. 创建连接
     */
    public void createChannel() {
        if(totalConnections.get() >= maxConnections) {
            System.err.println("超出最大连接池, 不再创建连接");
            return;
        }

        try {
            ChannelFuture cf = factory.createChannel();
            Channel channel = cf.sync().channel();

            channel.attr(LAST_USED_TIME).set(System.currentTimeMillis());

            idleConnections.add(channel);
            totalConnections.incrementAndGet();

        } catch (Exception e) {
            System.err.println("创建连接异常: " + e);
        }

    }

    /**
     * 3. 获取连接
     */
    public synchronized Channel borrowChannel() {
        if(totalConnections.get() == 0) {
            System.err.println("最大连接池数量不足，等待创建");
            initPool();
            System.out.println("最大连接池创建完成");
        }

        Channel channel = null;
        try {
            channel = idleConnections.take();
        } catch (Exception e) {
            System.err.println("获取连接失败");
            Thread.currentThread().interrupt();
        }

        if(channel!=null && channel.isActive()) {
            activeConnections.add(channel);
            return channel;
        }

        // 重新获取连接
        System.out.println("获得的连接已关闭，重新获取。。。");
        return this.borrowChannel();
    }

    /**
     * 4. 释放连接
     */
    public synchronized void returnChannel(Channel channel) {
        if(channel==null || !channel.isActive()) {
            this.closeChannel(channel);
            return;
        }

        // 判断池子满了没
        // 成功插入
        if(idleConnections.offer(channel)) {
            channel.attr(LAST_USED_TIME).set(System.currentTimeMillis());
            activeConnections.remove(channel);
        }
    }

    /**
     * 5. 关闭连接（面对连接dead的时候）
     */
    public synchronized void closeChannel(Channel channel) {
        if(channel != null) {
            try {
                channel.close().sync();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
            idleConnections.remove(channel);
            activeConnections.remove(channel);
            totalConnections.decrementAndGet();
        }
    }

    /**
     * 6. 填充连接池（当连接池不满的时候）
     */
    private void fillChannel() {
        System.err.println("连接池未满，开始填充, 当前情况: " + String.format("最大连接数: %d, 活跃连接数: %d, 空闲连接数: %d%n",
                totalConnections.get(),
                activeConnections.size(),
                idleConnections.size()));
        for (int i = idleConnections.size(); i < maxConnections; i++) {
            createChannel();
        }
        System.out.println("连接池填充完成");
    }

    /**
     * 7. 健康检查
     * （1）检查连接存活
     * （2）检查连接池是否满
     */
    public void healthCheck() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            checkIdleConnections();
            fillChannel();
            status();
        }, 10, 3, TimeUnit.SECONDS);
    }

    /**
     * 检查空间连接，如果超时不活跃，自动回收
     */
    private void checkIdleConnections() {
        long currentTime = System.currentTimeMillis();
        for (Channel channel : this.idleConnections) {
            Long lastUsedTime = channel.attr(LAST_USED_TIME).get();
            if (lastUsedTime!=null && currentTime - lastUsedTime > maxIdleTime) {
                // 超时回收
                System.err.println(channel + "被超时回收");
                idleConnections.remove(channel);
                closeChannel(channel);
            }
        }
    }

    public void status() {
        System.out.printf("最大连接数: %d, 活跃连接数: %d, 空闲连接数: %d%n",
                totalConnections.get(),
                activeConnections.size(),
                idleConnections.size()
                );
    }

}
