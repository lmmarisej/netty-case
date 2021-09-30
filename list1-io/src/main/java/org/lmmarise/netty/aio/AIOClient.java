package org.lmmarise.netty.aio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * AIO客户端
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 4:58 下午
 */
public class AIOClient {
    private static final Logger log = LoggerFactory.getLogger(AIOClient.class);

    private final AsynchronousSocketChannel client;

    public AIOClient() throws Exception {
        client = AsynchronousSocketChannel.open();
    }

    public void connect(String host, int port) {
        client.connect(new InetSocketAddress(host, port), null, new CompletionHandler<Void, Void>() {
            @Override
            public void completed(Void result, Void attachment) {
                try {
                    client.write(ByteBuffer.wrap("这是一条测试数据".getBytes())).get();
                    log.info("已发送至服务器");
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();
            }
        });
        final ByteBuffer bb = ByteBuffer.allocate(1024);
        client.read(bb, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                log.info("IO操作完成" + result);
                log.info("获取反馈结果" + new String(bb.array()));
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                log.error(exc.getMessage());
            }
        });

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException ex) {
            log.error(ex.getMessage());
        }

    }

    public static void main(String[] args) throws Exception {
        new AIOClient().connect("localhost", 8000);
    }
}
