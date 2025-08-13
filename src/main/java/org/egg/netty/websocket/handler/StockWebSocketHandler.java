package org.egg.netty.websocket.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.egg.netty.websocket.db.StockDatabase;
import org.egg.netty.websocket.entity.StockData;
import org.egg.netty.websocket.util.StockGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    // 存储所有连接的客户端
    private final ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final ObjectMapper mapper = new ObjectMapper();

    private final StockGenerator stockGenerator = new StockGenerator();


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx.channel());
        System.out.printf("客户端连接: %s%n", ctx.channel().remoteAddress());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        clients.remove(ctx.channel());
        System.out.printf("客户端断开: %s%n", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String request = msg.text();
//        Map<?, ?> map = mapper.readValue(request, Map.class);
        Map<String, String> map = mapper.readValue(request, new TypeReference<>() {});
        String type = map.get("type");
        switch (type) {
            case "subscribe" -> { // 订阅
                handleSubscribe(ctx, map);
            }
            case "unsubscribe" -> { // 订阅
                handleUnsubscribe(ctx, map);
            }
            case "history" -> { // 订阅
                handleHistory(ctx, map);
            }
            default -> sendError(ctx, "未知请求类型");
        }
    }

    private void handleHistory(ChannelHandlerContext ctx, Map<String, String> map) {
        String symbol = map.get("symbol");
        if(symbol==null || symbol.isEmpty()) {
            sendError(ctx, "缺少股票代码");
            return;
        }

        String days_string = map.get("days");
        int days = Integer.parseInt(days_string);
        if(days <=0 ) days = 7;

        List<StockData> list = StockDatabase.getHistoryData(symbol, days);

        Map<String, Object> response = new HashMap<>();
        response.put("type", "history");
        response.put("symbol", symbol);
        response.put("data", list);

        try {
            ctx.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(response)));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.close();
        }
    }

    private void handleUnsubscribe(ChannelHandlerContext ctx, Map<String, String> map) {
        String symbol = map.get("symbol");
        if(symbol==null || symbol.isEmpty()) {
            sendError(ctx, "缺少股票代码");
            return;
        }

        stockGenerator.unsubscribe(ctx.channel(), symbol);
        sendSuccess(ctx.channel(), "已取消订阅: " + symbol);
    }

    private void handleSubscribe(ChannelHandlerContext ctx, Map<String, String> map) {
        String symbol = map.get("symbol");
        if(symbol==null || symbol.isEmpty()) {
            sendError(ctx, "缺少股票代码");
            return;
        }

        stockGenerator.subscribe(ctx.channel(), symbol);
        sendSuccess(ctx.channel(), "已订阅: " + symbol);
    }

    private void sendSuccess(Channel channel, String msg) {
        Map<String, String > map = new HashMap<>();
        map.put("status", "success");
        map.put("message", msg);
        try {
            channel.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(map)));
        } catch (Exception e) {
            e.printStackTrace();
            channel.close();
        }
    }

    private void sendError(ChannelHandlerContext ctx, String msg) {
        Map<String, String > map = new HashMap<>();
        map.put("status", "error");
        map.put("message", msg);
        try {
            ctx.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(map)));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.close();
        }
    }
}
