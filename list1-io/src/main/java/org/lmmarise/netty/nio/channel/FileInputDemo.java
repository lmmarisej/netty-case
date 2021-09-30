package org.lmmarise.netty.nio.channel;

import java.io.FileInputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class FileInputDemo {

    static public void main(String[] args) throws Exception {
        URL url = FileInputDemo.class.getClassLoader().getResource("test.txt");
        FileInputStream fin = new FileInputStream(url.getFile());
        FileChannel fc = fin.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        fc.read(buffer);    // 从fc读入buffer
        buffer.flip();
        Charset charset = Charset.defaultCharset();
        while (buffer.remaining() > 0) {
            System.out.print((charset.decode(buffer)));
        }
        fin.close();
    }
}