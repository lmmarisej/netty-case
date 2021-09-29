package org.lmmarise.netty.tomcat.servlet;

import org.lmmarise.netty.tomcat.http.Request;
import org.lmmarise.netty.tomcat.http.Response;
import org.lmmarise.netty.tomcat.http.Servlet;

/**
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 12:34 下午
 */
public class SecondServlet extends Servlet {

    public void doGet(Request request, Response response) throws Exception {
        this.doPost(request, response);
    }

    public void doPost(Request request, Response response) throws Exception {
        response.write("This is second Servlet");
    }
    
}
