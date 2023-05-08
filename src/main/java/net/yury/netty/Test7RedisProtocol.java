package net.yury.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author yury757
 * redis协议：
 * set name zhangsan
 * *3             *表示后面有几块内容
 * $3             $表示下面这块内容的长度
 * set            具体内容
 * $4
 * name
 * $8
 * zhangsan
 */
@Slf4j
public class Test7RedisProtocol {
    public static void main(String[] args) throws InterruptedException {
        Bootstrap bs = new Bootstrap();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Channel channel = bs.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.debug(msg.toString());
                                        super.channelRead(ctx, msg);
                                    }
                                });
                    }
                })
                .connect(new InetSocketAddress("localhost", 6379))
                .sync()
                .channel();
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(1024);
        buffer.writeBytes("*3\r\n".getBytes())
                .writeBytes("$3\r\nset\r\n".getBytes())
                .writeBytes("$4\r\nname\r\n".getBytes())
                .writeBytes("$8\r\nzhangsan\r\n".getBytes());
        channel.writeAndFlush(buffer).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                channel.close().sync();
                workerGroup.shutdownGracefully();
            }
        });
    }
}
