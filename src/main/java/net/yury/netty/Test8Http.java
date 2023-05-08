package net.yury.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Test8Http {
    public static void main(String[] args) throws InterruptedException {
        int port = 8080;
        ServerBootstrap bs = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            bs.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new HttpServerCodec())
                                    // 如果只对某一种请求类型感兴趣，可以使用 SimpleChannelInboundHandler 带上指定类型，这样就只有指定类型的请求会走到这个handler
                                    .addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
                                            log.debug(msg.uri());
                                            DefaultFullHttpResponse response = new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
                                            byte[] resp = "<p>hello world</p>".getBytes(StandardCharsets.UTF_8);
                                            response.headers().setInt("Content-Length", resp.length);
                                            response.content().writeBytes(resp);
                                            ctx.channel().writeAndFlush(response);
                                        }
                                    });
//                                    .addLast(new ChannelInboundHandlerAdapter() {
//                                        @Override
//                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                            log.debug(msg.getClass().toString());
//                                            super.channelRead(ctx, msg);
//                                        }
//                                    });
                        }
                    })
                    .bind(new InetSocketAddress("0.0.0.0", port))
                    .sync()
                    .channel()
                    .closeFuture()
                    .sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
