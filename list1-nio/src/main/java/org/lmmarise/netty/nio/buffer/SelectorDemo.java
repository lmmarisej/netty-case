package org.lmmarise.netty.nio.buffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * @author lmmarise.j@gmail.com
 * @since 2021/9/28 6:31 下午
 */
public class SelectorDemo {
    private final int port = 8080;
    private final Selector selector = getSelector();
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);

    public SelectorDemo() throws IOException {
    }


    public static void main(String[] args) throws IOException {
        new SelectorDemo().listen();
    }

    // 注册事件
    private Selector getSelector() throws IOException {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);

        ServerSocket socket = server.socket();
        socket.bind(new InetSocketAddress(port));

        Selector sel = Selector.open();
        // 监听新连接 对于ServerSocketChannel来说也只有这个监听选项
        server.register(sel, SelectionKey.OP_ACCEPT);
        return sel;
    }

    // 开始监听
    public void listen() {
        System.out.println("port: " + port);
        try {
            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    process(key);   // 处理新连接到达事件
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void process(SelectionKey key) throws IOException {
        // 只有Server才监听该请求事件
        if (key.isAcceptable()) {
            System.out.println("连接");
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel channel = server.accept();    // 客户端的Channel
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        }
        // 读事件
        else if (key.isReadable()) {
            System.out.println("读");
            SocketChannel channel = (SocketChannel) key.channel();
            int len = channel.read(buffer);
            if (len > 0) {
                buffer.flip();
                String content = new String(buffer.array(), 0, len);
                SelectionKey sKey = channel.register(selector, SelectionKey.OP_WRITE);
                sKey.attach(content);
            } else {
                channel.close();
            }
            buffer.clear();
        }
        // 写事件
        else if (key.isWritable()) {
            System.out.println("写");
            SocketChannel channel = (SocketChannel) key.channel();
            String content = (String) key.attachment();
            ByteBuffer block = ByteBuffer.wrap(("输出内容：" + content).getBytes(StandardCharsets.UTF_8));
            channel.write(block);
        }
    }

}
