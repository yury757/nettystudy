package net.yury.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author yury757
 * 粘包、半包问题产生的原因：
 * 1、操作系统层面，在进行TCP传输时，有滑动窗口的缓冲区，当一次发送的字节数量太小，操作系统并不会直接发出去，而是会等攒一批再一起发送，或者某个数据包不够一个缓冲区去发送，于是被拆分到两个缓冲区中发送了
 * 2、应用程序层面，客户端和服务端之间进行数据传输时设置的缓冲区大小不一致
 */
@Slf4j
public class Test6StickyAndHalfPack {
    /**
     * netty解决粘包问题的几个方法（方法1、2效率较低）：
     * 1、{@link io.netty.handler.codec.LineBasedFrameDecoder} 以换行符为分隔符区分不同数据包
     * 2、{@link io.netty.handler.codec.DelimiterBasedFrameDecoder} 自定义分隔符区分不同数据包
     * 3、{@link io.netty.handler.codec.LengthFieldBasedFrameDecoder} 带长度字段的识别数据包长度的解码器
     * 下面以 LengthFieldBasedFrameDecoder 进行测试
     */
    public static void main(String[] args) {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                // 4字节表示内容长度+实际内容
                new LengthFieldBasedFrameDecoder(1024, 0, 4),
                new LoggingHandler(LogLevel.DEBUG)
        );

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);

        // 这是第一条消息
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
        buf.writeInt(content.length);
        buf.writeBytes(content);

        // 第二条消息虽然在同一个byteBuf中，但经过解码后，这就是第二条消息
        byte[] content2 = "im yury".getBytes(StandardCharsets.UTF_8);
        buf.writeInt(content2.length);
        buf.writeBytes(content2);

        embeddedChannel.writeInbound(buf);
    }
}
