package net.yury.netty;

import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * EmbeddedChannel可以用于测试handler
 */
@Slf4j
public class Test4EmbeddedChannel {
    public static void main(String[] args) {
        ChannelInboundHandler in1 = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.debug("in1");
                super.channelRead(ctx, msg);
            }
        };
        ChannelInboundHandler in2 = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.debug("in2");
                super.channelRead(ctx, msg);
            }
        };
        ChannelOutboundHandler out1 = new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                log.debug("out1");
                super.write(ctx, msg, promise);
            }
        };
        ChannelOutboundHandler out2 = new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                log.debug("out2");
                super.write(ctx, msg, promise);
            }
        };
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(in1, in2, out1, out2);
        // 模拟入站操作
        embeddedChannel.writeInbound("123");
        // 模拟出站操作
        embeddedChannel.writeOutbound("234");
    }
}
