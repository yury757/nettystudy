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
 * 重点：粘包、半包问题产生的原因：
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
     *
     * 注意1：LengthFieldBasedFrameDecoder 并不是自定义协议，而是解决粘包、半包的一种方式，这种方式可以适配大多数协议
     * netty在处理 LengthFieldBasedFrameDecoder 这个handler时，会按照给定参数将数据读完整，再传给下一个handler，下一个handler就可以按照自定义协议去编解码
     * 想一想这个问题：如果没有 LengthFieldBasedFrameDecoder 这个handler来处理粘包、半包，直接走到自定义协议handler
     * 除非你的自定义协议的 handler 使用 channel 并 while 循环一直读到你想要的字节长度，否则你的byteBuf很可能不是一个完整的数据帧，这样编解码就会出错。
     * 即 LengthFieldBasedFrameDecoder 这个handler是帮你做好了按照指定长度读到一个完整的数据帧
     *
     * 注意2：只要是会记录handler处理过程的一些状态，则这个handler是线程不安全的，不能被多个EventLoopGroup使用，比如 LengthFieldBasedFrameDecoder 就是线程不安全的，不能使用一个实例
     * 带 {@link io.netty.channel.ChannelHandler.Sharable} 注解的handler都是线程安全的
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
