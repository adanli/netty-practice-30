package org.egg.netty.chat.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.egg.netty.chat.UdpChatServer;
import org.egg.netty.chat.util.ChannelStore;

import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.util.Map;

public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private final Map<String, Long> clients;

    public UdpServerHandler(Map<String, Long> clients) {
        this.clients = clients;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        // 保存通道用于广播
        ChannelStore.setChannel(ctx.channel());

        String clientAddress = packet.sender().getAddress().getHostAddress() + ":" + packet.sender().getPort();
        String message = packet.content().toString(CharsetUtil.UTF_8);

        // 更新客户端活跃时间
        this.clients.put(clientAddress, System.currentTimeMillis());

        // 处理命令
        if(message.startsWith("/")) {
            handleCommand(clientAddress, message);
        } else {
            // 广播普通消息
            String displayMessage = clientAddress + ":" + message;
            System.out.println(displayMessage);
            UdpChatServer.broadcastMessage(displayMessage);
        }

    }

    private void handleCommand(String clientAddress, String command) {
        switch (command.toLowerCase()) {
            case "/join" -> {
                System.out.println("新用户加入: " + clientAddress);
                UdpChatServer.broadcastMessage("系统通知: " + clientAddress + " 加入了聊天室");

                sendWelcomeMessage(clientAddress);
            }
            case "/quit" -> {
                System.out.println("用户退出: " + clientAddress);
                clients.remove(clientAddress);
                UdpChatServer.broadcastMessage("系统通知: " + clientAddress + " 退出了聊天室");
            }
            case "/list" -> {
                // 用户列表
                sendUserList(clientAddress);
            }
            default -> throw new IllegalArgumentException("异常的指令");
        }
    }

    private void sendWelcomeMessage(String clientAddress) {
        String[] parts = clientAddress.split(":");
        InetSocketAddress address = new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));

        String welcome = String.format("""
                      欢迎加入聊天室! 当前在线人数: %d人
                      可用命令:
                      /list - 查看在线用户
                      /quit - 退出聊天室
                      """, clients.size());

        Channel channel = ChannelStore.getChannel();
        if(channel!=null && channel.isActive()) {
            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(welcome, CharsetUtil.UTF_8), address));
        }

    }

    private void sendUserList(String clientAddress) {
        String[] parts = clientAddress.split(":");
        InetSocketAddress address = new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));

        StringBuilder sb = new StringBuilder(String.format("在线用户(%d)人: %n", clients.size()));
        clients.forEach((k, v) -> {
            sb.append(" - ").append(k).append('\n');
        });

        Channel channel = ChannelStore.getChannel();
        if(channel!=null && channel.isActive()) {
            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(sb, CharsetUtil.UTF_8), address));
        }
    }



}
