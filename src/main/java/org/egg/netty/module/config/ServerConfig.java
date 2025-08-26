package org.egg.netty.module.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * 服务端配置
 */
public class ServerConfig {
    private final int bossThreads;
    private final int workerThreads;
    private final int businessThreads;
    private final int port;
    private final int soBacklog;
    private final boolean tcpNoDelay;
    private final boolean soKeepAlive;
    private final int writeBufferWaterMarkLow;
    private final int writeBufferWaterMarkHigh;

    private ServerConfig(Builder builder) {
        bossThreads = builder.bossThreads;
        workerThreads = builder.workerThreads;
        businessThreads = builder.businessThreads;
        port = builder.port;
        soBacklog = builder.soBacklog;
        tcpNoDelay = builder.tcpNoDelay;
        soKeepAlive = builder.soKeepAlive;
        writeBufferWaterMarkLow = builder.writeBufferWaterMarkLow;
        writeBufferWaterMarkHigh = builder.writeBufferWaterMarkHigh;

    }

    /**
     * 配置
     */
    public void applyTo(ServerBootstrap bootstrap) {
        bootstrap
                .option(ChannelOption.SO_BACKLOG, soBacklog)
                .childOption(ChannelOption.TCP_NODELAY, tcpNoDelay)
                .childOption(ChannelOption.SO_KEEPALIVE, soKeepAlive)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(writeBufferWaterMarkLow, writeBufferWaterMarkHigh))
        ;
    }

    /**
     * 创建业务线程池
     */
    public EventExecutorGroup createBusinessExecutor() {
        return new DefaultEventExecutorGroup(businessThreads);
    }

    public static class Builder {
        private int bossThreads = 1;
        private int workerThreads = Runtime.getRuntime().availableProcessors()*2;
        private int businessThreads = 16;
        private int port = 8000;
        private int soBacklog = 1024;
        private boolean tcpNoDelay = true;
        private boolean soKeepAlive = true;
        private int writeBufferWaterMarkLow = 32*1024;
        private int writeBufferWaterMarkHigh = 64*1024;

        public Builder setBossThreads(int bossThreads) {
            this.bossThreads = bossThreads;
            return this;
        }

        public Builder setWorkerThreads(int workerThreads) {
            this.workerThreads = workerThreads;
            return this;
        }

        public Builder setBusinessThreads(int businessThreads) {
            this.businessThreads = businessThreads;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setSoBacklog(int soBacklog) {
            this.soBacklog = soBacklog;
            return this;
        }

        public Builder setTcpNoDelay(boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
            return this;
        }

        public Builder setSoKeepAlive(boolean soKeepAlive) {
            this.soKeepAlive = soKeepAlive;
            return this;
        }

        public Builder setWriteBufferWaterMarkLow(int writeBufferWaterMarkLow) {
            this.writeBufferWaterMarkLow = writeBufferWaterMarkLow;
            return this;
        }

        public Builder setWriteBufferWaterMarkHigh(int writeBufferWaterMarkHigh) {
            this.writeBufferWaterMarkHigh = writeBufferWaterMarkHigh;
            return this;
        }

        public ServerConfig build() {
            return new ServerConfig(this);
        }
    }
}

