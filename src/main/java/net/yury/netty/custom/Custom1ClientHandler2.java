package net.yury.netty.custom;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.concurrent.TimeUnit;

public class Custom1ClientHandler2 extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext ctx;
    private ChannelPromise promise;
    private String response;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        String message = (String) msg;
//        if (message != null && message.getType() == TrayIcon.MessageType.SERVICE_RESP.value()) {
//            response = message;
//            promise.setSuccess();
//        } else {
//            ctx.fireChannelRead(msg);
//        }
//    }

    public synchronized ChannelPromise sendMessage(Object message) {
        while (ctx == null) {
            try {
                TimeUnit.MILLISECONDS.sleep(1);
                //logger.error("等待ChannelHandlerContext实例化");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        promise = ctx.newPromise();
        ctx.writeAndFlush(message);
        return promise;
    }

    public String getResponse(){
        return response;
    }
}
