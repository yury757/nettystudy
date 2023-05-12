package net.yury.netty.Test10Rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class RpcClient {
    public static void main(String[] args) throws InterruptedException {
        Bootstrap bs = new Bootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Channel channel = bs.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    // 日志
//                                    .addLast(new LoggingHandler(LogLevel.INFO))
                                    // 处理粘包、半包
                                    .addLast(new LengthFieldBasedFrameDecoder(2048, 0, 4))
                                    // 编解码
                                    .addLast(new RpcCodec(false))
                                    // 业务逻辑处理
                                    .addLast(new SimpleChannelInboundHandler<RpcResponseMessage>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
                                            if (msg.getCause() != null) {
                                                msg.getCause().printStackTrace();
                                            }else {
                                                log.debug("response is: {}", msg.getReturnValue());
                                            }
                                            ctx.channel().close();
                                        }
                                    });
                        }
                    })
                    .connect(new InetSocketAddress("localhost", 8080))
                    .sync()
                    .channel();

            RpcRequestMessage request = new RpcRequestMessage(
                    "net.yury.netty.Test10Rpc.service.HelloService",
                    "sayHello",
                    String.class,
                    new Class[] {String.class},
                    new Object[] {"你好"});
            channel.writeAndFlush(request);
            channel.closeFuture().sync();
        }finally {
            workerGroup.shutdownGracefully();
        }
    }
}
