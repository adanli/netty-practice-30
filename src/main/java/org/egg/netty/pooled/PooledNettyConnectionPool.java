package org.egg.netty.pooled;

import io.netty.channel.Channel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 负责管理连接池，提供连接申请、收回的功能
 * 连接：Channel
 */
public class PooledNettyConnectionPool {
    private static final Logger LOGGER = Logger.getLogger(PooledNettyConnectionPool.class.getName());
    private BlockingQueue<Channel> queue;
    /**
     * IP
     */
    private String host;
    /**
     * 端口
     */
    private int port;
    /**
     * 活跃的连接数量
     */
    private int activeConnections;
    /**
     * 空闲的连接数量
     */
    private int idleConnections;
    /**
     * 最小连接数量
     */
    private int minConnections;
    /**
     * 最大连接数量
     */
    private int maxConnections;

    private final PooledConnectionFactory pooledConnectionFactory;

    public PooledNettyConnectionPool(String host, int port, int minConnections, int maxConnections) {
        if(minConnections < 0) throw new IllegalArgumentException("minConnections blew than 0");
        if(maxConnections < 0) throw new IllegalArgumentException("maxConnections blew than 0");
        if(maxConnections < minConnections) throw new IllegalArgumentException("maxConnections less than minConnections");

        this.host = host;
        this.port = port;
        this.minConnections = minConnections;
        this.maxConnections = maxConnections;
        queue = new ArrayBlockingQueue<>(maxConnections);

        pooledConnectionFactory = new PooledConnectionFactory(host, port);

        initPool();

    }

    private void initPool() {
        for (int i = 0; i < minConnections; i++) {
           this.createConnection();
        }
    }


    public void createConnection() {
        try {
            Channel channel = pooledConnectionFactory.createChannel().sync().channel();
            queue.add(channel);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "创建连接失败", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 提供连接
     * 1. 如果连接池是空的，则等待
     */

    /**
     * 填充连接池
     */



    /**
     * 回收连接
     */

}
