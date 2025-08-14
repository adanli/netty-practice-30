package org.egg.netty.pool;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NettyConnectionPool {
    private static final Logger LOGGER = Logger.getLogger(NettyConnectionPool.class.getName());
    private static final AttributeKey<Long> LAST_USED_TIME = AttributeKey.valueOf("lastUsedTime");

    private final String host;
    private final int port;
    private final int maxConnections;
    private final int minConnections;
    private final long maxIdleTime;

    // 连接池状态
    private BlockingQueue<Channel> idleConnections;
    private Set<Channel> activeConnections;
    private final AtomicInteger totalConnections = new AtomicInteger();

    // 连接工厂
    private ConnectionFactory connectionFactory;

    // 健康检查调度器
    private ScheduledExecutorService healthCheckScheduler;


    public NettyConnectionPool(String host, int port, int maxConnections, int minConnections, long maxIdleTime) {
        this.host = host;
        this.port = port;
        this.maxConnections = maxConnections;
        this.minConnections = minConnections;
        this.maxIdleTime = maxIdleTime;

        this.idleConnections = new ArrayBlockingQueue<>(maxConnections);
        this.activeConnections = new HashSet<>();
        this.connectionFactory = new ConnectionFactory(host, port);

        initPool();

    }

    private void initPool() {
        for (int i = 0; i < minConnections; i++) {
            createConnection();
        }
        
        startHealthCheck();
        
    }

    private void startHealthCheck() {
        healthCheckScheduler = Executors.newSingleThreadScheduledExecutor();
        healthCheckScheduler.scheduleAtFixedRate(() -> {

            try {
                checkIdleConnections();
                checkMinConnections();
                logPoolStatus();

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "health check fail", e);
            }

        }, 30, 30, TimeUnit.SECONDS);


    }

    private void checkMinConnections() {
        while (totalConnections.get()<maxConnections && minConnections < maxConnections) {
            createConnection();
        }
    }

    private void createConnection() {
        if(totalConnections.get() > maxConnections) {
            throw new RuntimeException("超出最大的连接数量");
        }


        try {
            Channel channel = connectionFactory.createConnection().sync().channel();
            channel.attr(LAST_USED_TIME).set(System.currentTimeMillis());

            idleConnections.put(channel);
            totalConnections.incrementAndGet();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "创建Channel失败", e);
            Thread.currentThread().interrupt();
        }


    }

    /**
     * 获取连接
     */
    public Channel borrowConnection() throws InterruptedException{
        Channel channel = idleConnections.poll();

        if(channel==null && totalConnections.get()<maxConnections) {
            createConnection();
            channel = idleConnections.poll(5, TimeUnit.SECONDS);
        }

        // 如果还是没有获取到连接，就等待空闲连接
        if(channel == null) {
            channel = idleConnections.take();
        }

        // 检查是否有效
        if(!channel.isActive()) {
            closeConnection(channel);
            return borrowConnection();
        }

        // 转移集合
        idleConnections.remove(channel);
        activeConnections.add(channel);

        LOGGER.info("borrow channel: " + channel.id());
        return channel;
    }

    private void checkIdleConnections() {
        long now = System.currentTimeMillis();
        Iterator<Channel> iterator = idleConnections.iterator();

        while (iterator.hasNext()) {
            Channel channel = iterator.next();
            Long lastUsedTime = channel.attr(LAST_USED_TIME).get();

            if(lastUsedTime!=null && (lastUsedTime-now)>maxIdleTime) {
                // 超时断开连接
                if(idleConnections.remove(channel)) {
                    closeConnection(channel);
                    return;
                };
            }
        }
    }

    public void returnConnection(Channel channel) {
        if(channel==null ||!channel.isActive()) {
            closeConnection(channel);
            return;
        }

        // 更新时间
        channel.attr(LAST_USED_TIME).set(System.currentTimeMillis());

        // 如果连接池未满，归还到连接池
        if(idleConnections.size() < maxConnections) {
            if(idleConnections.offer(channel)) {
                LOGGER.info("returned channel: " + channel.id());
                return;
            }
        }

        // 连接池满，关闭连接
        LOGGER.log(Level.WARNING, "连接池满了，关闭连接");
        channel.close();
    }

    private void closeConnection(Channel channel) {
        if(channel != null) {
            try {
                if(channel.isActive()) {
                    channel.close().sync();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                activeConnections.remove(channel);
                idleConnections.remove(channel);
                totalConnections.decrementAndGet();
                LOGGER.info("close channel: " + channel.id());
            }
        }
    }

    public void shutdown() {
        if(healthCheckScheduler != null) {
            healthCheckScheduler.shutdown();
        }

        closeAllConnections();

    }

    private void closeAllConnections() {
        for (Channel channel: idleConnections) {
            closeConnection(channel);
        }

        for (Channel channel: activeConnections) {
            closeConnection(channel);
        }
    }

    private void logPoolStatus() {
        LOGGER.info(String.format("Connection pool status: Total=%d, Idle=%d, Active=%d%n",
                totalConnections.get(), idleConnections.size(), activeConnections.size()));
    }


}
