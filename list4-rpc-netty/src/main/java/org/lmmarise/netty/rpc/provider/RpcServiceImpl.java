package org.lmmarise.netty.rpc.provider;

import org.lmmarise.netty.rpc.api.RpcService;

/**
 * @author lmmarise.j@gmail.com
 * @since 2021/9/29 10:06 下午
 */
public class RpcServiceImpl implements RpcService {
    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public int sub(int a, int b) {
        return a - b;
    }

    @Override
    public int multi(int a, int b) {
        return a * b;
    }

    @Override
    public int div(int a, int b) {
        return a / b;
    }
}
