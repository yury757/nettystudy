package net.yury.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 多线程优化NIO服务器
 * 使用一个线程专门负责接收连接，并将连接上的SocketChannel注册到selector上，称为boss线程
 * 使用另外一两个线程专门处理IO事件，称为worker线程
 */
public class MultiThreadNIOServer {
    public final static int BUFFER_SIZE = 128;
    private int port;
    private int workerSize;

    public MultiThreadNIOServer(int port, int workerSize) {
        this.port = port;
        this.workerSize = workerSize;
    }

    public static void main(String[] args) throws IOException {
        MultiThreadNIOServer server = new MultiThreadNIOServer(8080, 2);
        server.start();
    }

    public void start() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress("0.0.0.0", port));

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT, null);
        List<Worker> workerList = new ArrayList<>();
        for (int i = 0; i < workerSize; i++) {
            Worker worker = new Worker("nio-worker-" + i);
            workerList.add(worker);
        }
        int index = 0;
        System.out.println("server start!");

        // boss线程其实就是这个main线程，也可以单独使用一个线程
        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey next = iterator.next();
                if (next.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel)next.channel();
                    SocketChannel sc = channel.accept();
                    System.out.println("[boss] new connection, channel: " + sc);
                    sc.configureBlocking(false);
                    Worker worker = workerList.get(index);
                    worker.receive(sc, SelectionKey.OP_READ, null);
                    index++;
                    if (index >= workerList.size()) {
                        index = 0;
                    }
                }
            }
        }
    }

    /**
     * worker线程，一个实例就是一个线程
     */
    static class Worker implements Runnable {
        private Selector workerSelector;
        private Thread thread;
        private String name;

        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

        public Worker(String name) throws IOException {
            this.name = name;
            this.thread = new Thread(this, this.name);
            this.workerSelector = Selector.open();
            this.thread.start();
        }

        public void receive(SocketChannel channel, int ops, Object att) {
            queue.add(() -> {
                try {
                    channel.register(this.workerSelector, ops, att);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            workerSelector.wakeup();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    workerSelector.select();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Runnable task = queue.poll();
                if (task != null) {
                    task.run();
                }
                Iterator<SelectionKey> iterator = workerSelector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey next = iterator.next();
                    if (next.isReadable()) {
                        SocketChannel channel = (SocketChannel)next.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(MultiThreadNIOServer.BUFFER_SIZE);
                        int size;
                        try {
                            size = channel.read(buffer);
                        }catch (IOException ex) {
                            ex.printStackTrace();
                            next.cancel();
                            continue;
                        }
                        if (size == -1) {
                            next.cancel();
                            continue;
                        }
                        buffer.flip();
                        String s = StandardCharsets.UTF_8.decode(buffer).toString();
                        System.out.println("[" + this.name + "] client send: " + s);
                        try {
                            channel.register(workerSelector, ~SelectionKey.OP_READ & SelectionKey.OP_WRITE, s);
                        } catch (ClosedChannelException e) {
                            e.printStackTrace();
                            next.cancel();
                        }
                    }else if (next.isWritable()) {
                        SocketChannel channel = (SocketChannel)next.channel();
                        String s = (String)next.attachment();
                        String response = "你好, you content is: " + s;
                        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
                        try {
                            while (buffer.hasRemaining()) {
                                channel.write(buffer);
                            }
                            channel.register(workerSelector, ~SelectionKey.OP_WRITE & SelectionKey.OP_READ, null);
                        } catch (IOException e) {
                            e.printStackTrace();
                            next.cancel();
                        }
                    }else {
                        next.cancel();
                    }
                }
            }
        }
    }
}

