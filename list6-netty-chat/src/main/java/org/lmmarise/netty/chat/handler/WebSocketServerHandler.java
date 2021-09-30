package org.lmmarise.netty.chat.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.EventExecutorGroup;
import org.lmmarise.netty.chat.processor.MsgProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lmmarise.j@gmail.com
 * @since 2021/9/30 8:37 下午
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(WebSocketServerHandler.class);

    private final MsgProcessor processor = new MsgProcessor();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        processor.sendMsg(ctx.channel(), msg.text());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel client = ctx.channel();
        String addr = processor.getAddress(client);
        log.info("WebSocket Client:" + addr + "异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }

}
