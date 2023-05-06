package net.yury.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yury757
 * 非阻塞网络IO的优点在于：
 * 1、write和read操作不会阻塞线程，若网络双方同时有IO需求，则可以进行IO读写，否则读写会返回的长度是0
 * 2、在非阻塞IO中，若遇到一个很大的IO操作占用很长时间，会导致影响其他客户端的IO，非阻塞IO可以根据需要将一次大的IO根据bytebuffer大小拆分为多次小的IO，这样不影响其他客户端。所以bytebuffer在NIO中极为重要
 *      需要分多次IO是因为操作系统中也有缓冲区，不可能说想分配多大的bytebuffer，就可以一次性读取或写入这么大的bytebuffer
 */
public class NIOServer {
    public final static int BUFFER_SIZE = 128;
    private int port;
    private AtomicInteger count = new AtomicInteger(0);

    public NIOServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws IOException {
        NIOServer nioServer = new NIOServer(8080);
        nioServer.start();
    }

    public void start() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress("0.0.0.0", port));

        ssc.register(selector, SelectionKey.OP_ACCEPT, null);
        System.out.println("server start!");

        while (true) {
            // 阻塞selector，当有事件发生时才会唤醒
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey next = iterator.next();
                // 注意：selector不会自动对已经处理完的selectionKey做删除操作，因此这里一定要移除这个selectionKey，不然下次又会对这个selectionKey做处理
                iterator.remove();
                if (next.isAcceptable()) {
                    System.out.println("accept event happen, selectionKey: " + next);
                    ServerSocketChannel channel = (ServerSocketChannel) next.channel();
                    SocketChannel sc = channel.accept();
                    System.out.println("new connection: " + sc);
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ, null);
                } else if (next.isReadable()) {
                    System.out.println("read event happen, selectionKey: " + next);
                    SocketChannel channel = (SocketChannel) next.channel();
                    // 注意：无论如何一定要对selectionKey做处理，要么read，要么cancel
                    // 客户端没有调用close，程序直接退出时，会报异常java.io.IOException: 远程主机强迫关闭了一个现有的连接。
                    // 客户端正常调用close时，会触发一个读事件，并且read返回-1
                    try {
                        channelReadLengthProtocol(channel, selector, next);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        // 注意：当报错时，认为客户端异常断开了，因此要将这个selectionKey取消掉
                        next.cancel();
                    }
                } else if (next.isWritable()) {
                    System.out.println("write event happen, selectionKey: " + next);
                    SocketChannel channel = (SocketChannel)next.channel();
                    channelWrite(channel, selector, next);
                }
            }
        }
    }

    public void channelRead(SocketChannel channel, Selector selector, SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        StringBuilder sb = new StringBuilder();
        // 注意：这里需要处理半包和粘包问题
        // 半包：一个数据包被拆分到多个buffer中，读取时没有将这些buffer组合在一起或组合的方式错误导致数据异常。
        // 解决方法1：使用attachment边扩容边多次读取，或在attachment中使用bytebuffer数组，可以避免bytebuffer拷贝；解决方法2：使用自定义协议，如channelReadLengthProtocol
        // 粘包：一个buffer中包含多个数据包，多个数据包混淆在一起，无法辨别哪个是哪个
        // 下面这个代码没有解决半包问题，在遇到中文时肯定会有乱码
        while (true) {
            int read = channel.read(buffer);
            if (read == -1) {
                // 注意：-1表示客户端正常断开，因此这里要将这个selectionKey取消掉
                key.cancel();
                return;
            } else if (read == 0) {
                /**
                 * 注意：返回值为0有三种情况：
                 * 1、由于操作系统缓冲区满了等其他原因，socketChannel中读到数据
                 * 2、byteBuffer的 position = limit
                 * 3、客户端发送数据完毕
                 */
                break;
            } else {
                // 注意：需要对buffer进行读操作时，一定要flip
                buffer.flip();
                String s = StandardCharsets.UTF_8.decode(buffer).toString();
                sb.append(s);
                // 注意：要重新对buffer进行写操作时，一定要clear，或者compact
                buffer.clear();
            }
        }
        String s = sb.toString();
        System.out.println("client send: " + s);
        channel.register(selector, ~SelectionKey.OP_READ & SelectionKey.OP_WRITE, s);
    }

    public void channelWrite(SocketChannel channel, Selector selector, SelectionKey key) throws IOException {
        String s = (String)key.attachment();
        ByteBuffer buffer = StandardCharsets.UTF_8.encode("你好, count: " + count.getAndIncrement() + ", your content length is " + s.length());
        // 注意：StandardCharsets.UTF_8.encode和ByteBuffer.wrap方法返回的buffer都是已经切到了读模式，不需要在flip，再flip一次反而把所有数据都弄丢了
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        channel.register(selector, ~SelectionKey.OP_WRITE & SelectionKey.OP_READ);
    }

    /**
     * 这是一个基于长度协议，并使用attachment分批读取的方式处理半包问题的解决方案
     * 主要解决办法就是在传输内容前加一个4字节数字表示剩余内容体的byte长度，然后服务端分批读取直到读取到对应长度的byte即可。
     * 客户端也需要按照这种协议去发送内容，否则可能会产生问题
     * @param channel
     * @param selector
     * @param key
     * @throws IOException
     */
    public void channelReadLengthProtocol(SocketChannel channel, Selector selector, SelectionKey key) throws IOException {
        Object attachment = key.attachment();
        AttachByteBuffer attachByteBuffer;
        if (attachment == null) {
            // 获取长度
            ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
            int size = 0;
            do {
                int tmp = channel.read(sizeBuffer);
                if (tmp == -1) {
                    key.cancel();
                    return;
                } else {
                    size += tmp;
                }
            } while (size != 4);
            // 获取内容的长度
            sizeBuffer.flip();
            int length = buffer2Int(sizeBuffer, 0);
            if (length <= 0) {
                System.out.println("unsupported content length: " + length);
                key.cancel();
                return;
            }

            // 初始化
            attachByteBuffer = new AttachByteBuffer(length, ByteBuffer.allocate(BUFFER_SIZE));
            key.attach(attachByteBuffer);
        }else {
            attachByteBuffer = (AttachByteBuffer)attachment;
        }

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int read = channel.read(buffer);
        System.out.println("read size: " + read);
        if (read == -1) {
            key.cancel();
            return;
        }
        attachByteBuffer.put(buffer);

        // 如果已经读完了，则取消读事件，注册写事件
        if (attachByteBuffer.getCurrentSize() == attachByteBuffer.getRequireSize()) {
            ByteBuffer completeBuffer = attachByteBuffer.getBuffer();
            completeBuffer.flip();
            String s = StandardCharsets.UTF_8.decode(completeBuffer).toString();
            System.out.println("client send length: " + s.length());
            channel.register(selector, ~SelectionKey.OP_READ & SelectionKey.OP_WRITE, s);
        }
    }

    /**
     * 读取bytebuffer中的4个字节，转int
     * 小端字节序
     * @param buffer
     * @param start
     * @return
     */
    public int buffer2Int(ByteBuffer buffer, int start) {
        int res = 0;
        for (int i = 0; i < 4; i++) {
            res += ((buffer.get(start + i) & 0xff) << (i * 8));
        }
        return res;
    }

    static class AttachByteBuffer {
        private int requireSize;
        private int currentSize;
        private ByteBuffer buffer;

        public AttachByteBuffer(int requireSize, ByteBuffer buffer) {
            this.requireSize = requireSize;
            this.currentSize = 0;
            this.buffer = buffer;
        }

        public int getRequireSize() {
            return requireSize;
        }

        public void setRequireSize(int requireSize) {
            this.requireSize = requireSize;
        }

        public ByteBuffer getBuffer() {
            return buffer;
        }

        public void setBuffer(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        public int getCurrentSize() {
            return currentSize;
        }

        public void setCurrentSize(int currentSize) {
            this.currentSize = currentSize;
        }

        public synchronized void put(ByteBuffer newBuffer) {
            newBuffer.flip();
            int size = newBuffer.limit() - newBuffer.position();
            // 如果buffer不够存储，则扩容
            if (buffer.limit() - buffer.position() < size) {
                ByteBuffer buffer2 = ByteBuffer.allocate(buffer.limit() + NIOServer.BUFFER_SIZE * 2);
                buffer.flip();
                buffer2.put(buffer);
                // 把旧的buffer替换了，旧的buffer已经没用了
                buffer = buffer2;
            }
            // 将新读取的数据写入到attachment中的buffer中
            this.currentSize += size;
            buffer.put(newBuffer);
        }
    }
}


