package org.lmmarise.netty.tomcat.http;

import java.io.OutputStream;

/**
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 12:27 下午
 */
public class Response {
    private final OutputStream out;

    public Response(OutputStream out) {
        this.out = out;
    }

    public void write(String s) throws Exception {
        // 用的是HTTP协议，输出也要遵循HTTP协议
        // 给到一个状态码 200
        String sb = "HTTP/1.1 200 OK\n" +
                "Content-Type: text/html;\n" +
                "\r\n" + s;
        out.write(sb.getBytes());
    }
}
