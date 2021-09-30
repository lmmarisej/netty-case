package org.lmmarise.netty.chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.lmmarise.netty.chat.handler.HttpServerHandler;
import org.lmmarise.netty.chat.handler.TerminalServerHandler;
import org.lmmarise.netty.chat.handler.WebSocketServerHandler;
import org.lmmarise.netty.chat.protocol.IMDecoder;
import org.lmmarise.netty.chat.protocol.IMEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lmmarise.j@gmail.com
 * @since 2021/9/30 6:32 下午
 */
public class ChatServer {
    private static final Logger log = LoggerFactory.getLogger(ChatServer.class);
    private final int port = 80;

    public static void main(String[] args) {
        new ChatServer().start();
    }

    private void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)     // 服务端需要指定两个EventLoopGroup，用于处理连接请求和连接后的IO
                    .channel(NioServerSocketChannel.class)  // 用于服务端的SocketChannel
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(socketChannelChannelInitializer());
            ChannelFuture f = b.bind(port).sync();
            log.info("服务已启动，监听端口：" + this.port);
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ChannelInitializer<SocketChannel> socketChannelChannelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                // 解析自定义协议
                pipeline.addLast(new IMDecoder());              // Inbound
                pipeline.addLast(new IMEncoder());              // Outbound
                pipeline.addLast(new TerminalServerHandler());  // Inbound

                // 解析Http请求
                pipeline.addLast(new HttpServerCodec());  // Outbound
                // 主要是将同一个 http 请求或响应的多个消息对象变成一个 fullHttpRequest 完整的消息对象
                pipeline.addLast(new HttpObjectAggregator(64 * 1024)); // Inbound
                // 主要用于处理大数据流,比如一个1G大小的文件如果你直接传输肯定会撑暴jvm内存的,加上这个handler我们就不用考虑这个问题了
                pipeline.addLast(new ChunkedWriteHandler());    // Inbound、Outbound
                pipeline.addLast(new HttpServerHandler());      // Inbound

                // 解析WebSocket请求
                pipeline.addLast(new WebSocketServerProtocolHandler("/im"));    // Inbound
                pipeline.addLast(new WebSocketServerHandler());     // Inbound
            }
        };
    }

}
