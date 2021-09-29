package org.lmmarise.netty.tomcat;

import org.lmmarise.netty.tomcat.http.Request;
import org.lmmarise.netty.tomcat.http.Response;
import org.lmmarise.netty.tomcat.http.Servlet;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * 基于 bio 的 web 服务器。
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 12:26 下午
 */
public class Tomcat {
    private final int port = 8080;
    private ServerSocket server;
    private final Map<String, Servlet> servletMapping = new HashMap<>();
    private final Properties webConf = new Properties();
    private final ExecutorService executor = new ThreadPoolExecutor(10, 10,
            60L, TimeUnit.SECONDS, new ArrayBlockingQueue(10));

    /**
     * 加载 web.properties（web.xml）文件，同时初始化 ServletMapping 对象
     */
    private void init() {
        try {
            String WEB_INF = this.getClass().getResource("/").getPath();
            FileInputStream fis = new FileInputStream(WEB_INF + "web.properties");
            webConf.load(fis);
            for (Object k : webConf.keySet()) {
                String key = k.toString();
                if (key.endsWith(".url")) {
                    String servletName = key.replaceAll("\\.url$", "");
                    String url = webConf.getProperty(key);
                    String className = webConf.getProperty(servletName + ".className");
                    // 单实例，多线程
                    Servlet obj = (Servlet) Class.forName(className).newInstance();
                    servletMapping.put(url, obj);   // url 与 Servlet 映射
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Java 通过 native 方法调用操作系统的 API 来在 Socket 中进行数据的传输。
     */
    public void start() {
        init();     // 初始化工作，加载配置
        try {
            server = new ServerSocket(this.port);
            System.out.println("Tomcat 已启动，监听的端口：" + this.port);
            while (true) {          // 等待用户请求
                // 接收来自客户端的Socket连接请求
                Socket client = server.accept();    // 调用PlainSocketImpl#socketAccept方法阻塞，到系统接收到连接请求后通知恢复
                process(client);    // HTTP 请求
                executor.submit(() -> processWrapperWithException(client));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 统一处理 process 过程中的异常
     */
    private void processWrapperWithException(Socket client) {
        try {
            process(client);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void process(Socket client) throws Exception {
        InputStream is = client.getInputStream();
        OutputStream os = client.getOutputStream();

        Request request = new Request(is);
        Response response = new Response(os);

        String url = request.getUrl();
        if (servletMapping.containsKey(url)) {  // 匹配 Servlet
            servletMapping
                    .get(url)
                    .service(request, response);    // 处理业务逻辑
        } else {
            response.write("404 - Not Found");
        }

        os.flush(); // 不管管道里面数据有没有满，都会推送数据出去
        os.close(); // 先关闭输出端
        is.close();

        client.close(); // 关闭socket
    }

    /**
     * 启动自己写的 web 服务器
     */
    public static void main(String[] args) {
        new Tomcat().start();
    }
}
