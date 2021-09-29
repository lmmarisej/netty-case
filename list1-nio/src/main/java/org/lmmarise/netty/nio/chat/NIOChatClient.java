package org.lmmarise.netty.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class NIOChatClient {
    private final InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8080);
    private Selector selector = null;
    private SocketChannel client = null;

    private String nickName = "";
    private final Charset charset = StandardCharsets.UTF_8;
    private static final String USER_EXIST = "系统提示：该昵称已经存在，请换一个昵称";
    private static final String USER_CONTENT_SPLIT = "#@#";


    public NIOChatClient() throws IOException {
        selector = Selector.open();
        //连接远程主机的IP和端口
        client = SocketChannel.open(serverAddress);
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    public void session() {
        //开辟一个新线程从服务器端读数据
        new Reader().start();
        //开辟一个新线程往服务器端写数据
        new Writer().start();
    }

    private class Writer extends Thread {

        @Override
        public void run() {
            try {
                //在主线程中 从键盘读取数据输入到服务器端
                Scanner scan = new Scanner(System.in);
                while (scan.hasNextLine()) {
                    String line = scan.nextLine();
                    if ("".equals(line)) continue; //不允许发空消息
                    if ("".equals(nickName)) {
                        nickName = line;
                        line = nickName + USER_CONTENT_SPLIT;
                    } else {
                        line = nickName + USER_CONTENT_SPLIT + line;
                    }
//		            client.register(selector, SelectionKey.OP_WRITE);
                    client.write(charset.encode(line));//client既能写也能读，这边是写
                }
                scan.close();
            } catch (Exception ignored) {
            }
        }
    }

    private class Reader extends Thread {
        public void run() {
            try {
                while (true) {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) continue;
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();  //可以通过这个方法，知道可用通道的集合
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        process(key);
                    }
                }
            } catch (IOException ignored) {
            }
        }

        private void process(SelectionKey key) throws IOException {
            if (key.isReadable()) {
                //使用 NIOServerDemoBak 读取 Channel中的数据，这个和全局变量client是一样的，因为只注册了一个SocketChannel
                //client既能写也能读，这边是读
                SocketChannel sc = (SocketChannel) key.channel();

                ByteBuffer buff = ByteBuffer.allocate(1024);
                StringBuilder content = new StringBuilder();
                while (sc.read(buff) > 0) {
                    buff.flip();
                    content.append(charset.decode(buff));
                }
                //若系统发送通知名字已经存在，则需要换个昵称
                if (USER_EXIST.equals(content.toString())) {
                    nickName = "";
                }
                System.out.println(content);
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new NIOChatClient().session();
    }
}
