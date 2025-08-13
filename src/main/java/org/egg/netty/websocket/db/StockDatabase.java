package org.egg.netty.websocket.db;

import org.egg.netty.websocket.entity.StockData;
import org.egg.netty.websocket.util.StockGenerator;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 股票数据库
 */
public class StockDatabase {
    private static final Map<String, List<StockData>> HISTORY_DATA = new HashMap<>();
    private static final Random RANDOM = new Random();

    static {
        // 初始化数据
        long now = System.currentTimeMillis();

        for (String symbol: StockGenerator.stock_symbols) {
            List<StockData> list = new ArrayList<>();
            double price = 100 + RANDOM.nextDouble() * 900;

            for (int i = 0; i < 30; i++) {
                // 生成30天数据
                double change = (RANDOM.nextDouble()-0.5) * 20;
                price = Math.max(1, price+change);

                StockData stockData = new StockData(
                        symbol,
                        String.format("%.2f", price),
                        String.format("%.2f", change),
                        String.format("%.2f", change*100/(price-change)),
                        now - TimeUnit.DAYS.toMillis(30-i)
                );
                list.add(stockData);

            }

            HISTORY_DATA.put(symbol, list);

        }

    }

    public static List<StockData> getHistoryData(String symbol, int days) {
        List<StockData> list = HISTORY_DATA.get(symbol);
        if(list == null) return new ArrayList<>();

        // 返回最近days天的数据
        int start = Math.max(0, list.size() - days);
        return list.subList(start, list.size());
    }

}
