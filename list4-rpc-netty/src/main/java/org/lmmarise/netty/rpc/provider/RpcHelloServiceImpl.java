package org.lmmarise.netty.rpc.provider;

import org.lmmarise.netty.rpc.api.RpcHelloService;

/**
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 10:06 下午
 */
public class RpcHelloServiceImpl implements RpcHelloService {
    @Override
    public String hello(String name) {
        System.out.println("RpcHelloServiceImpl#hello(" + name + ")");
        return "Hello " + name + ".";
    }
}
