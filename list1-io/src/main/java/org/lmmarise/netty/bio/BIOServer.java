package org.lmmarise.netty.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 同步的阻塞 IO 模型
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 5:00 下午
 */
public class BIOServer {
    ServerSocket server;        // 服务端网络IO模型的封装对象

    public BIOServer(int port) {    // 启动服务并监听端口
        try {
            server = new ServerSocket(port);
            System.out.println("BIO服务已启动，监听端口是：" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听并处理逻辑
     */
    public void listen() throws IOException {
        while (true) {
            //等待客户端连接，阻塞方法
            //Socket数据发送者在服务端的引用
            Socket client = server.accept();
            System.out.println(client.getPort());

            //对方法数据给我了，读 Input
            InputStream is = client.getInputStream();

            //网络客户端把数据发送到网卡，机器所得到的数据读到了JVM内中
            byte[] buff = new byte[1024];
            int len = is.read(buff);
            if (len > 0) {
                String msg = new String(buff, 0, len);
                System.out.println("收到" + msg);
            }
        }
    }


    public static void main(String[] args) throws IOException {
        new BIOServer(8080).listen();
    }

}
