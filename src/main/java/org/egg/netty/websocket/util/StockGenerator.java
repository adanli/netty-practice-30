package org.egg.netty.websocket.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 股票数据生成器
 */
public class StockGenerator {
    // 每个客户端订阅的股票
    public static final Map<Channel, Set<String>> SUBSCRIPTIONS = new ConcurrentHashMap<>();
    // 每个股票的当前价格
    private final Map<String, Double> CURRENT_PRICE = new ConcurrentHashMap<>();
    // 股票列表
    public static final String[] stock_symbols = {
            "AAPL", "GOOGL", "MSFT", "AMZN", "TSLA",
            "FB", "NFLX", "NVDA", "BABA", "JPM"
    };

    private final ObjectMapper mapper = new ObjectMapper();

    public StockGenerator() {
        // 初始化价格
        Random random = new Random();
        for (String stock_symbol: stock_symbols) {
            CURRENT_PRICE.put(stock_symbol, random.nextDouble() * 900);
        }

        // 启动定时任务推送数据
        EventLoopGroup group = new DefaultEventLoopGroup();
        group.scheduleAtFixedRate(this::generateData, 1, 1, TimeUnit.SECONDS);

    }

    public void subscribe(Channel channel, String symbol) {
//        SUBSCRIPTIONS.computeIfAbsent(channel, k -> new HashSet<>()).add(symbol);
        Set<String> symbols = SUBSCRIPTIONS.computeIfAbsent(channel, k -> new HashSet<>());
        symbols.add(symbol);

        System.out.println("订阅完成, 股票编号: " + symbol);
    }

    public void unsubscribe(Channel channel, String symbol) {
        Set<String> symbols = SUBSCRIPTIONS.get(channel);
        if(symbols != null) {
            symbols.remove(symbol);
            if(symbols.isEmpty()) {
                SUBSCRIPTIONS.remove(channel);
                System.out.println("取消订阅完成, 股票编号: " + symbol);
            }
        }
    }

    private void generateData() {
        Random random = new Random();
        for (Map.Entry<Channel, Set<String>> entry: SUBSCRIPTIONS.entrySet()) {
            Channel channel = entry.getKey();
            Set<String> symbols = entry.getValue();

            Map<String, Object> map = new HashMap<>();
            map.put("type", "update");

            Map<String, String> data = new HashMap<>();
            map.put("data", data);


            for (String symbol: symbols) { // 股票
                double currentPrice = CURRENT_PRICE.get(symbol);
                double change = (random.nextDouble()-0.5) * 10; // -5 ~ 5
                double newPrice = currentPrice + change;

                if(newPrice < 1) newPrice = 1;

                CURRENT_PRICE.put(symbol, newPrice);

                data.put("symbol", symbol);
                data.put("price", String.format("%.2f", newPrice));
                data.put("change", String.format("%.2f", change));
                data.put("changePercent", String.format("%.2f", change*100/currentPrice));
                data.put("timestamp", System.currentTimeMillis()+"");

            }

            if(channel.isActive()) {
                try {
                    channel.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(map)));
                } catch (Exception e) {
                    e.printStackTrace();
                    channel.close();
                }
            }

        }


    }

}
