package org.lmmarise.netty.tomcat.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

/**
 * 利用了Netty对HTTP的支持
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 12:27 下午
 */
public class Response {
    private ChannelHandlerContext ctx;  // SocketChannel的封装
    private HttpRequest req;

    public Response(ChannelHandlerContext ctx, HttpRequest req) {
        this.ctx = ctx;
        this.req = req;
    }

    public void write(String out) {
        try {
            if (out == null || out.length() == 0) {
                return;
            }
            // 设置 http 协议及请求头信息
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(out.getBytes(StandardCharsets.UTF_8))
            );
            response.headers().set("Content-Type", "text/html;");
            ctx.write(response);
        } finally {
            ctx.flush();
            ctx.close();
        }
    }
}
