package org.lmmarise.netty.aio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AIO服务端
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 4:59 下午
 */
public class AIOServer {
    private static final Logger log = LoggerFactory.getLogger(AIOServer.class);

    private final int port;

    public static void main(String[] args) {
        int port = 8000;
        new AIOServer(port);
    }

    public AIOServer(int port) {
        this.port = port;
        listen();
    }

    private void listen() {
        try {
            ExecutorService executorService = Executors.newCachedThreadPool();
            AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withCachedThreadPool(executorService, 1);
            //开门营业
            //工作线程，用来侦听回调的，事件响应的时候需要回调
            final AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(threadGroup);
            server.bind(new InetSocketAddress(port));
            log.info("服务已启动，监听端口" + port);
            //准备接受数据
            server.accept(null, completionHandler(server));
            try {
                Thread.sleep(Integer.MAX_VALUE);
            } catch (InterruptedException ex) {
                log.error(ex.getMessage());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private CompletionHandler<AsynchronousSocketChannel, Object> completionHandler(AsynchronousServerSocketChannel server) {
        return new CompletionHandler<AsynchronousSocketChannel, Object>() {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

            // 实现completed方法来回调  由操作系统来触发  回调有两个状态，成功
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                log.info("IO操作成功，开始获取数据");
                try {
                    buffer.clear();
                    result.read(buffer).get();
                    buffer.flip();
                    result.write(buffer);
                    buffer.flip();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    try {
                        result.close();
                        server.accept(null, this);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                log.info("操作完成");
            }

            @Override
            //回调有两个状态，失败
            public void failed(Throwable exc, Object attachment) {
                log.error("IO操作是失败: " + exc);
            }
        };
    }
}
