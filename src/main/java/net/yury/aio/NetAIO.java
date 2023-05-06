package net.yury.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;

/**
 * 代码还有点问题，会报错
 */
public class NetAIO {

    public static void main(String[] args) throws IOException {
        // 是不是很像netty的handler
        CompletionHandler<Integer, ByteBuffer> handler = new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                attachment.flip();
                System.out.println(StandardCharsets.UTF_8.decode(attachment).toString());
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.out.println("error1");
                exc.printStackTrace();
            }
        };

        try (AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel.open()) {
            listener.bind(new InetSocketAddress("0.0.0.0", 8080));

            listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, ByteBuffer>() {
                @Override
                public void completed(AsynchronousSocketChannel channel, ByteBuffer attachment) {
                    listener.accept(null, this);
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    channel.read(buffer, buffer, handler);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.out.println("error2");
                    exc.printStackTrace();
                }
            });
        }catch (IOException ex) {
            System.out.println("error3");
            ex.printStackTrace();
        }
        System.in.read();
    }

}
