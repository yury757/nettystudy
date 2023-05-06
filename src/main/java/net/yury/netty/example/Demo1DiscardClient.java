package net.yury.netty.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class Demo1DiscardClient {
    public static final Logger LOGGER = LoggerFactory.getLogger(Demo1DiscardClient.class);

    public static void main(String[] args) throws InterruptedException {
        Bootstrap bs = new Bootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Channel channel = bs.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8080))
                .sync()
                .channel();

        channel.writeAndFlush("123")
                .addListener(future -> {
                    LOGGER.info("channel is " + channel);
                    channel.close().sync();
                    workerGroup.shutdownGracefully();
                });
    }
}
