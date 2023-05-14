package net.yury.netty.Test10Rpc.client;

import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import net.yury.netty.Test10Rpc.RpcCodec;
import net.yury.netty.Test10Rpc.RpcResponseMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcClientHandlerInitializer extends ChannelInitializer<Channel> {
    public static final Map<Long, Promise<Object>> PROMISE_MAP = new ConcurrentHashMap<>();

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline()
                // 日志
//                .addLast(new LoggingHandler(LogLevel.INFO))
                // 处理粘包、半包
                .addLast(new LengthFieldBasedFrameDecoder(2048, 0, 4))
                // 编解码
                .addLast(new RpcCodec(false))
                // 业务逻辑处理
                .addLast(new SimpleChannelInboundHandler<RpcResponseMessage>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
                        long id = msg.getSequenceID();
                        Promise<Object> promise = PROMISE_MAP.getOrDefault(id, null);
                        if (promise == null) {
                            throw new RuntimeException("promise is null, please check your program");
                        }
                        // 一定别忘了remove
                        PROMISE_MAP.remove(id);
                        if (msg.getCause() != null) {
                            promise.setFailure(msg.getCause());
                        }else {
                            promise.setSuccess(msg.getReturnValue());
                        }
                    }
                });
    }
}
