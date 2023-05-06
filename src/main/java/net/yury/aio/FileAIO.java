package net.yury.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 异步IO是指数据的等待和拷贝都交由另外一个线程执行，再给另外一个线程一个回调函数，告诉他数据准备好了就执行这个回调函数，我这边就可以得到响应。
 */
public class FileAIO {
    public static void main(String[] args) {
        try(AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("test.txt"),StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            System.out.println("[" + Thread.currentThread().getName() + "] begin read");
            channel.read(buffer, 0, null, new CompletionHandler<Integer, ByteBuffer>() {

                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    buffer.flip();
                    System.out.println("[" + Thread.currentThread().getName() + "] file content is: " + StandardCharsets.UTF_8.decode(buffer).toString());
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                }
            });
            System.out.println("[" + Thread.currentThread().getName() + "] finish read");
            System.in.read();
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
