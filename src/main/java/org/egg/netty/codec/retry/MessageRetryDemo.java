package org.egg.netty.codec.retry;

/**
 * 支持消息重发机制的网络通信案例
 *
 * 协议格式：
 * +----------------+----------------+----------------+----------------+
 * |  魔数 (4字节)   |  消息类型 (1)   |  序列号长度 (1) |  序列号 (变长)   |
 * +----------------+----------------+----------------+----------------+
 * |                        数据内容 (变长)                         |
 * +---------------------------------------------------------------+
 *
 * 消息类型：
 * 0x01: 业务请求
 * 0x02: 业务响应
 * 0x03: 心跳请求
 * 0x04: 心跳响应
 */
public class MessageRetryDemo {
    public static final int PORT = 8080;
    public static final int MAGIC_NUMBER = 0x12345678;
    public static final long REQUEST_TIMEOUT_MS = 2000; // 2秒超时
    public static final int MAX_RETRIES = 3; // 最大重试次数

    public static void main(String[] args) {

    }

}
