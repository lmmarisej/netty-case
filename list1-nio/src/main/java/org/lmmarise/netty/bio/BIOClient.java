package org.lmmarise.netty.bio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

/**
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 5:00 下午
 */
public class BIOClient {

    public static void main(String[] args) throws IOException {
        // 要和谁进行通信，服务器IP、服务器的端口
        Socket client = new Socket("localhost", 8080);
        // 不管是客户端还是服务端，都有可能 write 和 read
        OutputStream os = client.getOutputStream();
        String name = UUID.randomUUID().toString(); // 生成一个随机的ID
        System.out.println("客户端发送数据：" + name);
        os.write(name.getBytes());  // 101011010
        os.close();
        client.close();
    }

}
