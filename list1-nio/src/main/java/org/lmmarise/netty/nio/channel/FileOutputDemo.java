package org.lmmarise.netty.nio.channel;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileOutputDemo {
    static private final byte[] message = {83, 111, 109, 101, 32, 98, 121, 116, 101, 115, 46};

    static public void main(String[] args) throws Exception {
        URL url = FileInputDemo.class.getClassLoader().getResource("test.txt");
        FileOutputStream fout = new FileOutputStream(url.getFile());
        FileChannel fc = fout.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        for (byte b : message) {
            buffer.put(b);
        }
        buffer.flip();
        fc.write(buffer);
        fout.close();
    }
}