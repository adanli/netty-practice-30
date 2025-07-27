package org.egg.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class NioEchoClient {
    private final ByteBuffer read = ByteBuffer.allocate(1024);
    private final ByteBuffer write = ByteBuffer.allocate(1024);
    private final static int PORT = 8088;

    public static void main(String[] args) {
        new NioEchoClient().execute();
    }

    private void execute() {
        try (SocketChannel socketChannel = SocketChannel.open();
             Selector selector = Selector.open();
        ){
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress("localhost", PORT));
            socketChannel.register(selector, SelectionKey.OP_CONNECT);

            while (selector.select() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if(key.isConnectable() && key.channel() instanceof SocketChannel client) {
                        if(client.finishConnect()) {
                            System.out.println("connect success");

                            client.register(selector, SelectionKey.OP_READ);

                            write.put("hello, i am client".getBytes(Charset.defaultCharset()));
                            write.flip();
                            client.write(write);
                            write.clear();

                        }
                    } else if(key.isReadable() && key.channel() instanceof SocketChannel client) {
                        int r = client.read(read);
                        if(r > 0) {
                            read.flip();
                            byte[] b = new byte[read.limit()];
                            read.get(b);
                            System.out.println(new String(b, Charset.defaultCharset()));

                            read.clear();

                        } else if(r < 0) {
                            key.cancel();
                            System.out.println("server stopped");
                        }
                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
