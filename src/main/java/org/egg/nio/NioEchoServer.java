package org.egg.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class NioEchoServer {
    private final ByteBuffer read = ByteBuffer.allocate(1024);
    private final ByteBuffer write = ByteBuffer.allocate(1024);
    private final static int PORT = 8088;

    public static void main(String[] args) {
        new NioEchoServer().execute();
    }

    private void execute() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             Selector selector = Selector.open();
        ){
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (selector.select() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if(key.isAcceptable()) {
                        // 拿到Client，发送回执，创建连接
                        if(key.channel() instanceof ServerSocketChannel server) {
                            server.configureBlocking(false);

                            SocketChannel client = server.accept();
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                            write.put("hello, i am server".getBytes(Charset.defaultCharset()));
                            write.flip();
                            client.write(write);

                            write.clear();
                        }


                    } else if(key.isReadable()) {
                        if(key.channel() instanceof SocketChannel client) {

                            int r = client.read(read);

                            if(r > 0) {
                                read.flip();
                                byte[] b = new byte[read.limit()];
                                read.get(b);
                                System.out.printf(new String(b, Charset.defaultCharset()));

                                read.clear();

                                write.put("from server: ".getBytes(Charset.defaultCharset()));
                                write.put(b);
                                write.flip();
                                client.write(write);
                                write.clear();

                            } else if(r < 0) {
                                key.cancel();
                                System.out.println("stop connect");
                            }

                        }
                    }


                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        ;
    }

}
