package org.egg.netty.udp.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


/**
 * 接收到消息，发送广播
 * 消息内容:
 *  /list: 显示所有成员信息
 *  /quit: 退出聊天室
 *  ...: 正常聊天
 */
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    // <IP:PORT, LastActiveTimestamp>
    private final Map<String, Long> clients;

    public UdpServerHandler(Map<String, Long> clients) {
        this.clients = clients;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        // 客户端地址
        String host = packet.sender().getAddress().getHostAddress();
        int port = packet.sender().getPort();
        String clientAddress = String.format("%s:%s", host, port);

        // 客户端消息
        ByteBuf buf = packet.content();
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes, 0, buf.readableBytes());

        String message = new String(bytes, CharsetUtil.UTF_8);
        System.out.println("服务端接收到消息: " + message);


        switch (message.toLowerCase()) {
            case "/join" -> {
                String string = String.format("["+sdf.format(new Date())+"]"+": 系统消息: " + clientAddress + " 加入了聊天室");
                clients.put(clientAddress, System.currentTimeMillis());
                this.broadcastMessage(ctx.channel(), Unpooled.copiedBuffer(string.getBytes(CharsetUtil.UTF_8)));
            }
            case "/list" -> {
                StringBuilder sb = new StringBuilder("["+sdf.format(new Date())+"]"+"显示全部用户: %n");
                clients.forEach((k, v) -> {
                    sb.append(" - ").append(k).append('\n');
                });
                this.sendMessage(ctx.channel(), clientAddress, Unpooled.copiedBuffer(sb.toString().getBytes(CharsetUtil.UTF_8)));
            }
            case "/quit" -> {
                String str = "["+sdf.format(new Date())+"]"+"系统消息: " + clientAddress + " 退出了聊天室";
                clients.remove(clientAddress);
                this.sendMessage(ctx.channel(), clientAddress, Unpooled.copiedBuffer(str.getBytes(CharsetUtil.UTF_8)));
            }
            case "beat" -> { // 心跳
                clients.put(clientAddress, System.currentTimeMillis());
            }
            default -> {
                ByteBuf byteBuf = Unpooled.copiedBuffer(("["+sdf.format(new Date())+"]"+message).getBytes(CharsetUtil.UTF_8));
                this.broadcastMessage(ctx.channel(), byteBuf);
            }
        }



    }

    private void sendMessage(Channel channel, String clientAddress, ByteBuf buf) {
        String[] parts = clientAddress.split(":");
        InetSocketAddress address = new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
        channel.writeAndFlush(new DatagramPacket(buf, address));
    }

    private void broadcastMessage(Channel channel, ByteBuf buf) {
        clients.forEach((k, v) -> {
            this.sendMessage(channel, k, buf.retainedDuplicate());
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("服务端异常");
        ctx.close();
    }
}
