package org.lmmarise.netty.rpc.registry;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import  org.lmmarise.netty.rpc.protocol.InvokerProtocol;

import static java.util.Objects.requireNonNull;

/**
 * 将 Provider 接口通过 Netty 暴露
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 10:18 下午
 */
public class RegistryHandler extends ChannelInboundHandlerAdapter {
    public static Map<Object, Object> registryMap = new ConcurrentHashMap<>();    // 保存所有可用的服务
    private final List<String> classNames = new ArrayList<>();        // 保存所有相关的服务类

    public RegistryHandler() {
        scannerClass("org.lmmarise.netty.rpc.provider");    // 递归扫描
        doRegister();
    }

    /**
     * 当客户端建立连接时，需要从自定义协议中获取信息，拿到具体的服务和实参
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        InvokerProtocol protocol = (InvokerProtocol) msg;   // 请求对象即协议
        if (registryMap.containsKey(protocol.getClassName())) {
            Object clazz = registryMap.get(protocol.getClassName());
            Method method = clazz.getClass().getMethod(protocol.getMethodName(), protocol.getParams());
            result = method.invoke(clazz, protocol.getValues());     // 反射调用请求资源，得到结果
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 将 Class 实例化，以其接口为 key，注册到 registryMap
     */
    private void doRegister() {
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                Class<?> inter = clazz.getInterfaces()[0];
                registryMap.put(inter.getName(), clazz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 扫描包名（文件夹）下所有 Class，将其权限类名存入 classNames
     */
    private void scannerClass(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File dir = new File(requireNonNull(url).getFile());
        for (File classFile : requireNonNull(dir.listFiles())) {
            if (classFile.isDirectory()) {
                scannerClass(packageName + "." + classFile.getName());
            } else {
                classNames.add(packageName + "." + classFile.getName().replace(".class", "").trim());
            }
        }
    }
}
