package net.yury.netty.Test10Rpc;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class RpcServerHandlerInitializer extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline()
                // 日志
                .addLast(new LoggingHandler(LogLevel.INFO))
                // 处理粘包、半包
                .addLast(new LengthFieldBasedFrameDecoder(2048, 0, 4))
                // 编解码
                .addLast(new RpcCodec())
                // 业务逻辑处理
                .addLast(new RpcServerHandler());
    }
}
