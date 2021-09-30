package org.lmmarise.netty.rpc.comsumer;

import org.lmmarise.netty.rpc.api.RpcHelloService;
import org.lmmarise.netty.rpc.api.RpcService;
import org.lmmarise.netty.rpc.comsumer.proxy.RpcProxy;

/**
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 11:15 下午
 */
public class RpcConsumer {

    public static void main(String[] args) {
        RpcHelloService rpcHello = RpcProxy.create(RpcHelloService.class);
        RpcService service = RpcProxy.create(RpcService.class);

        System.out.println(rpcHello.hello("蔡徐坤老师"));
//        System.out.println("8 + 2 = " + service.add(8, 2));
//        System.out.println("8 - 2 = " + service.sub(8, 2));
//        System.out.println("8 * 2 = " + service.multi(8, 2));
//        System.out.println("8 / 2 = " + service.div(8, 2));
    }

}
