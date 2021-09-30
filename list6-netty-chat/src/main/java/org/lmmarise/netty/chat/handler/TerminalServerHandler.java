package org.lmmarise.netty.chat.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.lmmarise.netty.chat.processor.MsgProcessor;
import org.lmmarise.netty.chat.protocol.IMMessage;

/**
 * @author lmmarise.j@gmail.com
 * @since 2021/9/30 7:01 下午
 */
public class TerminalServerHandler extends SimpleChannelInboundHandler<IMMessage> {
    private final MsgProcessor processor = new MsgProcessor();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMMessage msg) {
        processor.sendMsg(ctx.channel(), msg);
    }
}
