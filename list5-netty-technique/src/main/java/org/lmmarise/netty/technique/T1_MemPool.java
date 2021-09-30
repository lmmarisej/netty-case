package org.lmmarise.netty.technique;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

/**
 * 内存池缓冲区重用机制
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/30 4:25 下午
 */
public class T1_MemPool {

    /**
     * 内存池分配技术
     */
    public static void pooled() {
        final byte[] content = new byte[1024];
        long start = System.currentTimeMillis();
        ByteBuf poolBuffer;
        for (int i = 0; i < 1_800_000; i++) {
            poolBuffer = PooledByteBufAllocator.DEFAULT.directBuffer(1024);
            poolBuffer.writeBytes(content);
            poolBuffer.release();
        }
        long end = System.currentTimeMillis();
        System.out.println("内存池分配用时，" + (end - start) + "ms");
    }

    /**
     * 非堆内存分配器创建直接缓冲区
     */
    public static void unPooled() {
        final byte[] content = new byte[1024];
        long start = System.currentTimeMillis();
        ByteBuf poolBuffer;
        for (int i = 0; i < 1_800_000; i++) {
            poolBuffer = Unpooled.directBuffer(1024);
            poolBuffer.writeBytes(content);
            poolBuffer.release();
        }
        long end = System.currentTimeMillis();
        System.out.println("非内存池分配用时，" + (end - start) + "ms");
    }

    public static void main(String[] args) {
        pooled();
        unPooled();
        pooled();
        unPooled();
        pooled();
        unPooled();
        pooled();
        unPooled();
    }

}
