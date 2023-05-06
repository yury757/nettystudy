package net.yury.netty.custom;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * 自定义TCP报文协议编码器，返回结果时使用
 * 前四个字节代表int型长度，表示该请求的报文长度（不包含int4个字节本身），后面字节都是UTF-8型的字符串
 */
public class Custom1Encoder extends MessageToByteEncoder<String> {
    public static final Custom1Encoder instance = new Custom1Encoder();

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        System.out.println("[encoder] msg: " + msg);
        out.writeBytes(Custom1Decoder.HEAD);
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
