package org.lmmarise.netty.rpc.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * 注册中心
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 10:09 下午
 */
public class RpcRegistry {
    private final int port;

    public RpcRegistry(int port) {
        this.port = port;
    }

    public void start() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                    .group(bossGroup, workerGroup)          // 表示NIO的EventLoopGroup
                    .channel(NioServerSocketChannel.class)  // 指定Channel类型
                    .childHandler(channelInitializer())     // 设置处理数据的handler
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("RPC Registry started listen at " + port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private ChannelInitializer<SocketChannel> channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("encoder", new ObjectEncoder());
                pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE,
                        ClassResolvers.cacheDisabled(null)));
                pipeline.addLast(new RegistryHandler());
            }
        };
    }

    public static void main(String[] args) {
        new RpcRegistry(9000).start();
    }

}
