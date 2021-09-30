package org.lmmarise.netty.rpc.comsumer.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 封装 Netty 调用后的返回数据
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 11:16 下午
 */
public class RpcProxyHandler extends ChannelInboundHandlerAdapter {
    private Object response;

    public Object getResponse() {
        return response;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        response = msg;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("client exception is general");
    }
}
