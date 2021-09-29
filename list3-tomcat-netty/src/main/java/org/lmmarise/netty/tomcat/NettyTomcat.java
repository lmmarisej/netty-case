package org.lmmarise.netty.tomcat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.lmmarise.netty.tomcat.http.Request;
import org.lmmarise.netty.tomcat.http.Response;
import org.lmmarise.netty.tomcat.http.Servlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Netty就是一个同时支持多协议的网络通信框架
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 5:20 下午
 */
public class NettyTomcat {
    private static final Logger log = LoggerFactory.getLogger(NettyTomcat.class);

    private final int port = 8081;
    private final Map<String, Servlet> servletMapping = new HashMap<>();
    private final Properties webConf = new Properties();

    /**
     * 加载配置文件，初始化 ServletMapping 对象
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
                    Servlet obj = (Servlet) Class.forName(className).newInstance();
                    servletMapping.put(url, obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        init();
        EventLoopGroup bossGroup = new NioEventLoopGroup();     // boss线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();   // Worker线程
        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)      // 主线程处理类，看到这样的写法，底层就是用反射
                    .childHandler(new ChannelInitializer<SocketChannel>() {     // 子线程处理类, Handler
                        protected void initChannel(SocketChannel client) {     // 客户端初始化处理
                            // 无锁化串行编程  // Netty对HTTP协议的封装，顺序有要求    // 责任链模式，双向链表 Inbound OutBound
                            client.pipeline().addLast(new HttpResponseEncoder());   // HttpResponseEncoder 编码器
                            client.pipeline().addLast(new HttpRequestDecoder());    // HttpRequestDecoder 解码器
                            client.pipeline().addLast(new TomcatHandler());     // 业务逻辑处理
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)      // 针对主线程的配置 分配线程最大数量 128
                    .childOption(ChannelOption.SO_KEEPALIVE, true);     // 针对子线程的配置 保持长连接
            ChannelFuture f = server.bind(port).sync();     // 启动服务器
            System.out.println("Tomcat 已启动，监听端口：" + port);
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully(); // 关闭线程池
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 利用了 Netty 对 HTTP 的支持，将 Netty 接收到的请求封装客户端 Socket，交给 tomcat 来处理。
     */
    public class TomcatHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest) {
                HttpRequest req = (HttpRequest) msg;
                Request request = new Request(ctx, req);        // 转交给我们自己的request实现
                Response response = new Response(ctx, req);     // 转交给我们自己的response实现
                String url = request.getUrl();
                if (servletMapping.containsKey(url)) {
                    servletMapping.get(url).service(request, response);     // 实际业务处理
                } else {
                    response.write("404 - Not Found");
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        }
    }

    public static void main(String[] args) {
        new NettyTomcat().start();
    }
}
