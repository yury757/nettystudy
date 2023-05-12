package net.yury.netty.Test10Rpc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import net.yury.netty.Test10Rpc.service.RpcRegisterProcessor;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    private static final AtomicInteger COUNT = new AtomicInteger(0);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) {
        log.debug("receive msg is: {}", msg);
        String interfaceName = msg.getInterfaceName();
        Object result = null;
        RpcResponseMessage resp = new RpcResponseMessage();
        try {
            Class<?> clazz = Class.forName(interfaceName);
            Method method = clazz.getMethod(msg.getMethodName(), msg.getParamsTypes());
            result = method.invoke(RpcRegisterProcessor.RPC_SERVICE.get(clazz).get(1), msg.getParamsValues());
            resp.setReturnValue(result);
            resp.setCause(null);
        }catch (Throwable cause) {
//            Throwable[] suppressed = cause.getSuppressed();
//            for (Throwable throwable : suppressed) {
//
//            }
//            while ()
            cause.printStackTrace();
            resp.setCause(cause);
        }
        // 处理业务逻辑
        ctx.channel().writeAndFlush(resp);
    }
}
