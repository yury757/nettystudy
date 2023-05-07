package net.yury.netty.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class EchoClient {
    public static void main(String[] args) throws InterruptedException, IOException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Channel channel = bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new StringDecoder())
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.debug("receive msg is: " + msg.toString());
                                        super.channelRead(ctx, msg);
                                    }
                                })
                                .addLast(new StringEncoder());
                    }
                })
                .connect("localhost", 8080)
                .sync()
                .channel();
        ChannelFuture future = channel.writeAndFlush("你好");
        future.sync();
        DefaultChannelPromise promise = (DefaultChannelPromise) future;
        System.out.println("return msg is: " + promise.getNow());
        channel.close().sync();
        workerGroup.shutdownGracefully();
    }
}
