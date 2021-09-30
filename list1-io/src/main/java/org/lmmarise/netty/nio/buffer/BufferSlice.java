package org.lmmarise.netty.nio.buffer;

import java.nio.ByteBuffer;

/**
 * 缓冲区分片
 */
public class BufferSlice {
    static public void main(String[] args) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        for (int i = 0; i < buffer.capacity(); ++i) buffer.put((byte) i);   // 初始化为0~9
        // 创建子缓冲区
        buffer.position(3); // 起始位置
        buffer.limit(7);    // 结束
        ByteBuffer slice = buffer.slice();
        // 改变子缓冲区的内容
        for (int i = 0; i < slice.capacity(); ++i) {
            slice.put(i, (byte) (slice.get(i) * 10));   //  下标 3 ~ 6 会被改变
        }
        // 打印整个数组上数据
        buffer.position(0);
        buffer.limit(buffer.capacity());
        while (buffer.remaining() > 0) {
            System.out.println(buffer.get());
        }
    }
}