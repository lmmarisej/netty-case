package org.lmmarise.netty.tomcat.http;

import java.io.InputStream;

/**
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 12:27 下午
 */
public class Request {
    private String method;
    private String url;

    public Request(InputStream in) {
        try {
            String content = "";
            byte[] buff = new byte[1024];
            int len = 0;
            if ((len = in.read(buff)) > 0) {
                content = new String(buff, 0, len);
            }

            // 按照HTTP协议解析数据
            String line = content.split("\\n")[0];
            String[] arr = line.split("\\s");

            this.method = arr[0];
            this.url = arr[1].split("\\?")[0];
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

}
