package net.yury.netty.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

public class Demo1DiscardServer {
    public static void main(String[] args) throws InterruptedException {
        int port = 8080;
        ServerBootstrap bs = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        bs.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new StringDecoder())
                                .addLast(new DiscardServerHandler());
                    }
                })
                .bind(new InetSocketAddress("0.0.0.0", port)).sync().channel().closeFuture().sync();
    }
}
