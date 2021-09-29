package org.lmmarise.netty.tomcat.http;

/**
 * J2EE标准
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 12:27 下午
 */
public abstract class Servlet {

    public void service(Request request, Response response) throws Exception {
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            doGet(request, response);
        } else {
            doPost(request, response);
        }
    }

    public abstract void doGet(Request request, Response response) throws Exception;

    public abstract void doPost(Request request, Response response) throws Exception;
}
