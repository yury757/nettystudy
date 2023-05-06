package net.yury.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author yury757
 */
public class NIOClient {
    public static void main(String[] args) throws IOException {
        client1();
    }

    public static void client1() throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            str.append("a");
        }
        byte[] content = str.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(content);
        while (buffer.hasRemaining()) {
            sc.write(buffer);
        }

        buffer = ByteBuffer.allocate(1024);
        sc.read(buffer);
        buffer.flip();
        System.out.println("server response is: " + StandardCharsets.UTF_8.decode(buffer).toString());
        sc.close();
    }

    public static void client2() throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 50000; i++) {
            str.append("你");
        }
        byte[] content = str.toString().getBytes(StandardCharsets.UTF_8);
        int size = content.length;
        ByteBuffer buffer = ByteBuffer.allocate(size + 4);
        byte[] sizeByteArray = int2Byte(size);
        // 先写长度，再写内容
        buffer.put(sizeByteArray);
        buffer.put(content);

        buffer.flip();
        while (buffer.hasRemaining()) {
            int temp = sc.write(buffer);
            System.out.println("send size: " + temp);
        }

        // 接收端一样处理，省略
        StringBuilder sb = new StringBuilder();
        buffer = ByteBuffer.allocate(2);
        while (sc.read(buffer) > 0) {
            buffer.flip();
            sb.append(StandardCharsets.UTF_8.decode(buffer));
            buffer.clear();
        }
        System.out.println("server response is: " + sb.toString());
        sc.close();
    }

    /**
     * int转byte数组
     * 小段字节序
     * @param number
     * @return
     */
    public static byte[] int2Byte(int number) {
        byte[] res = new byte[4];
        for (int i = 0; i < 4; i++) {
            res[i] = (byte)(number & 0xff);
            number = number >> 8;
        }
        return res;
    }
}
