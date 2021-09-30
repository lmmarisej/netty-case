package org.lmmarise.netty.rpc.api;

/**
 * 模拟业务加减乘除计算
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 10:00 下午
 */
public interface RpcService {

    int add(int a, int b);

    int sub(int a, int b);

    int multi(int a, int b);

    int div(int a, int b);

}
