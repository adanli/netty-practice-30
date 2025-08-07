package org.egg.netty.chat.util;

import io.netty.channel.Channel;

public class ChannelStore {
    private static volatile Channel channel;

    public static Channel getChannel() {
        return channel;
    }

    public static void setChannel(Channel channel) {
        ChannelStore.channel = channel;
    }
}
