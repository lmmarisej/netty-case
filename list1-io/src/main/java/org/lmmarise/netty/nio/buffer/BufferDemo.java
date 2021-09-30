package org.lmmarise.netty.nio.buffer;

import java.io.FileInputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BufferDemo {

    public static void main(String[] args) throws Exception {
        URL url = BufferDemo.class.getClassLoader().getResource("test.txt");
        assert url != null;
        FileInputStream fin = new FileInputStream(url.getFile());
        FileChannel fc = fin.getChannel();      // 创建文件的操作管道
        ByteBuffer buffer = ByteBuffer.allocate(10);    // 分配一个10个大小缓冲区
        output("初始化", buffer);
        fc.read(buffer);
        output("调用read()", buffer);
        buffer.flip();
        output("调用flip()", buffer);
        // 判断有没有可读数据
        while (buffer.remaining() > 0) {
            byte b = buffer.get();
            System.out.print(((char) b));
        }
        output("调用get()", buffer);
        buffer.clear();
        output("调用clear()", buffer);
        fin.close();
    }

    public static void output(String step, ByteBuffer buffer) {
        System.out.println(step + " : ");
        System.out.print("capacity: " + buffer.capacity() + ", ");
        System.out.print("position: " + buffer.position() + ", ");
        System.out.println("limit: " + buffer.limit()); // 数据操作范围索引只能在position - limit 之间
        System.out.println();
    }
}