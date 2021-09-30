package org.lmmarise.netty.rpc.comsumer.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.lmmarise.netty.rpc.protocol.InvokerProtocol;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 通过代理+Netty，实现远程方法调用
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 11:16 下午
 */
public class RpcProxy {
    private static final String providerHost = "localhost";
    private static final int providerPort = 9000;

    /**
     * 为接口或类上的接口创建代理对象
     */
    public static <T> T create(Class<T> clazz) {
        MethodProxy proxy = new MethodProxy(clazz);
        Class<?>[] interfaces = clazz.isInterface() ? new Class[]{clazz} : clazz.getInterfaces();
        return clazz.cast(Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, proxy));
    }

    private static class MethodProxy implements InvocationHandler {
        private final Class<?> clazz;

        public <T> MethodProxy(Class<T> clazz) {
            this.clazz = clazz;
        }

        /**
         * 代理规则
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (Object.class.equals(method.getDeclaringClass())) {   // 传进来是一个已实现的具体类
                try {
                    return method.invoke(this, args);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else {
                return rpcInvoke(proxy, method, args);
            }
            return null;
        }

        /**
         * 通过 Netty 远程调用指定 Provider 的指定方法
         */
        private Object rpcInvoke(Object proxy, Method method, Object[] args) {
            InvokerProtocol msg = new InvokerProtocol();    // 传输协议封装
            msg.setClassName(this.clazz.getName());
            msg.setMethodName(method.getName());
            msg.setValues(args);
            msg.setParams(method.getParameterTypes());

            final RpcProxyHandler consumerHandler = new RpcProxyHandler();
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group).channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(channelInitializer(consumerHandler));
                ChannelFuture future = bootstrap.connect(providerHost, providerPort).sync();
                future.channel().writeAndFlush(msg).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                group.shutdownGracefully();
            }

            return consumerHandler.getResponse();
        }

        /**
         * maxFrameLength       框架的最大长度。如果帧的长度大于此值，则将抛出TooLongFrameException。
         * lengthFieldOffset    长度字段的偏移量：即对应的长度字段在整个消息数据中得位置
         * lengthFieldLength    长度字段的长度：如：长度字段是int型表示，那么这个值就是4（long型就是8）
         * lengthAdjustment     要添加到长度字段值的补偿值
         * initialBytesToStrip  从解码帧中去除的第一个字节数
         */
        private <T extends SocketChannel> ChannelInitializer<T> channelInitializer(ChannelHandler handler) {
            return new ChannelInitializer<T>() {
                @Override
                public void initChannel(T ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                            0, 4, 0, 4));   // 自定义协议解码器
                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));  // 自定义协议编码器
                    pipeline.addLast("encoder", new ObjectEncoder());   // 对象参数类型编码器
                    pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE,
                            ClassResolvers.cacheDisabled(null)));   // 对象参数类型解码器
                    pipeline.addLast("handler", handler);
                }
            };
        }
    }
}
