package org.lmmarise.netty.nio.buffer;

import java.nio.ByteBuffer;

/**
 * 只读缓冲区
 */
public class ReadOnlyBuffer {
    static public void main(String[] args) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        for (int i = 0; i < buffer.capacity(); ++i) buffer.put((byte) i);
        ByteBuffer readonly = buffer.asReadOnlyBuffer();    // 创建只读缓冲区
        for (int i = 0; i < buffer.capacity(); ++i) {       // 原缓冲区的内容改变
            buffer.put(i, (byte) (buffer.get(i) * 10));
        }
        readonly.position(0);
        readonly.limit(buffer.capacity());
        while (readonly.remaining() > 0) { // 随着源缓冲区的改变只读缓冲区的内容也随之改变
            System.out.println(readonly.get());
        }
    }
}