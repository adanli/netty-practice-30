package org.egg.netty.websocket.entity;

public record StockData(
        String symbol, // 编号
        String price, // 价格
        String change, // 变化
        String changePercent, // 变化率
        long timestamp // 时间
) {
}
