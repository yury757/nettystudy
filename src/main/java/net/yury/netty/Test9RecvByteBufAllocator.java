package net.yury.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yury757
 * ByteBuf的分配器有两种：
 * 一种是普通ByteBuf的分配器，主要有pooled堆内存、unpooled堆内存、pooled直接内存、unpooled直接内存四种
 * 另外一种是直接接收网络IO的ByteBuf的分配器，有 {@link RecvByteBufAllocator}，具体是pooled还是unpooled还是由 io.netty.allocator.type 参数控制，并强制使用了直接内存
 * RecvByteBufAllocator 的作用是控制netty接收缓冲区的大小
 */
@Slf4j
public class Test9RecvByteBufAllocator {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 指定 RCVBUF_ALLOCATOR 为自己设置的 allocator
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(128, 1024, 4096))
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            log.debug("receive msg: {}", msg);
                                            System.out.println("123");
                                            super.channelRead(ctx, msg);
                                        }
                                    });
                        }
                    });
            ChannelFuture f = b.bind(8080).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
